# Integration Recipes — OTLP (Tempo) + Loki with Logged

This page shows end-to-end recipes to wire Logged with OpenTelemetry (Tempo/OTLP) and ship logs to Loki, enabling log-trace correlation in Grafana. Pick the scenario that fits your environment.

---

## 1) Local Dev — Java Agent + Tempo (OTLP)

Requirements
- OpenTelemetry Java Agent (download `opentelemetry-javaagent.jar`)
- Tempo listening on OTLP gRPC `4317` (see Docker Compose below) or use SaaS endpoint

Run Spring Boot with agent
```bash
java \
  -javaagent:/path/opentelemetry-javaagent.jar \
  -Dotel.service.name=demo-logged \
  -Dotel.exporter.otlp.endpoint=http://localhost:4317 \
  -Dotel.traces.exporter=otlp \
  -Dotel.metrics.exporter=none \
  -Dotel.resource.attributes=deployment.environment=dev,service.version=local \
  -jar build/libs/app.jar
```

Notes
- The Logged OpenTelemetry plugin (`logged.otel.*`) enriches spans with method/class metadata.
- Keep `logged.otel.enabled=true` to activate the plugin.

---

## 2) Docker Compose — App + Tempo + Loki + Promtail + Grafana

Docker Compose (minimal, adjust versions/paths)
```yaml
version: "3.9"
services:
  app:
    image: your/app:latest
    environment:
      OTEL_SERVICE_NAME: demo-logged
      OTEL_EXPORTER_OTLP_ENDPOINT: http://tempo:4317
      OTEL_TRACES_EXPORTER: otlp
      OTEL_METRICS_EXPORTER: none
      JAVA_TOOL_OPTIONS: "-javaagent:/otel/opentelemetry-javaagent.jar"
      # Spring Logged config (examples)
      LOGGED_OTEL_ENABLED: "true"
      LOGGED_SLF4J_ENABLED: "true"
    volumes:
      - ./otel/opentelemetry-javaagent.jar:/otel/opentelemetry-javaagent.jar:ro
    depends_on: [tempo]
    command: ["java","-jar","/app/app.jar"]

  tempo:
    image: grafana/tempo:2.6.0
    command: ["-config.file=/etc/tempo/config.yaml"]
    volumes:
      - ./tempo/config.yaml:/etc/tempo/config.yaml:ro
    ports: ["4317:4317", "3200:3200"]

  loki:
    image: grafana/loki:2.9.4
    command: ["-config.file=/etc/loki/config.yaml"]
    volumes:
      - ./loki/config.yaml:/etc/loki/config.yaml:ro
    ports: ["3100:3100"]

  promtail:
    image: grafana/promtail:2.9.4
    command: ["-config.file=/etc/promtail/config.yaml"]
    volumes:
      - ./promtail/config.yaml:/etc/promtail/config.yaml:ro
    depends_on: [loki]

  grafana:
    image: grafana/grafana:11.1.0
    ports: ["3000:3000"]
    depends_on: [loki, tempo]
```

Promtail (scrape app stdout)
```yaml
server:
  http_listen_port: 9080
clients:
  - url: http://loki:3100/loki/api/v1/push
scrape_configs:
  - job_name: container-logs
    static_configs:
      - targets: [app]
        labels:
          job: app
          host: app
          __path__: /var/log/containers/*.log
# For Compose stdout, either use the docker plugin that exposes container logs as files,
# or log to a file in the container and point __path__ to it.
```

Grafana
- Add data sources: Loki (http://loki:3100), Tempo (http://tempo:3200)
- Explore logs and traces; use derived fields to link `trace_id` to Tempo.

---

## 3) Kubernetes — Sidecarless (Java Agent)

Deployment snippet
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: demo-logged
spec:
  template:
    spec:
      containers:
        - name: app
          image: your/app:latest
          env:
            - name: OTEL_SERVICE_NAME
              value: demo-logged
            - name: OTEL_EXPORTER_OTLP_ENDPOINT
              value: http://otel-collector:4317
            - name: OTEL_TRACES_EXPORTER
              value: otlp
            - name: OTEL_METRICS_EXPORTER
              value: none
            - name: JAVA_TOOL_OPTIONS
              value: "-javaagent:/otel/opentelemetry-javaagent.jar"
            - name: LOGGED_OTEL_ENABLED
              value: "true"
          volumeMounts:
            - name: otel-agent
              mountPath: /otel
      volumes:
        - name: otel-agent
          configMap: { name: otel-javaagent }
```

Ship logs to Loki
- Option A: Promtail DaemonSet scrapes container stdout.
- Option B: use a Logback JSON appender to push directly to Loki (advanced).

---

## 4) Log Format for Correlation (Logback)

Include MDC + return/exception details. Example pattern:
```properties
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level trace=%X{trace_id} span=%X{span_id} svc=%X{service.name} %X{class}.%X{method} outcome=%X{outcome} %msg%n
```

Notes
- Some Java agent versions inject `trace_id`/`span_id` into MDC automatically; if not, add your own MDC injection or appender.
- Logged’s MDC plugin writes `class`, `method`, `args`, `result`, `outcome`, etc. See docs/ref/plugin-mdc.md.

---

## 5) Logged Settings That Help

- OpenTelemetry plugin (spans):
```yaml
logged:
  otel:
    enabled: true
    addArgs: true
    addReturnType: true
    addToMdc: true
    mdcKey: span_name
```

- SLF4J plugin (human logs):
```yaml
logged:
  slf4j:
    enabled: true
    callMsgArgs:   "{h:}{cI:} {c}::{m} args: [{a}]"
    returnMsg:     "{h:}{rI:} {c}::{m} returned."
    returnMsgValue: "{h:}{rI:} {c}::{m} returned: {rV}"
    exceptionMsg:  "{h:}{exI:} {c}::{m} threw {ex}: {eM}"
```

---

## 6) Troubleshooting

- No spans in Tempo: verify OTEL endpoint/ports and that the Java agent is loaded (`-javaagent`). Check service name.
- Missing trace_id in logs: your agent/appender may not inject MDC automatically; add MDC injection or use an Otel appender.
- High cardinality: avoid logging huge arg/return values; tune `logged.maxValueLength`, `argValues`, and masking.
- Source line missing: not all runtimes expose `ProceedingJoinPoint#getSourceLocation()`; consider disabling `addSourceLine` if noisy.
