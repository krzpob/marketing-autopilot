#!/bin/bash
set -e

BOOTSTRAP=kafka:9092              # internal listener, nie external
TOPICS_CMD=/opt/kafka/bin/kafka-topics.sh   # pełna ścieżka w apache/kafka
REPLICATION=1
PARTITIONS=3

create_topic() {
  local name=$1
  local partitions=${2:-$PARTITIONS}
  local retention_ms=${3:-604800000}

  $TOPICS_CMD \
    --bootstrap-server $BOOTSTRAP \
    --create \
    --if-not-exists \
    --topic "$name" \
    --partitions "$partitions" \
    --replication-factor $REPLICATION \
    --config retention.ms="$retention_ms"

  echo "✓ $name"
}

echo "Czekam na Kafkę..."
until $TOPICS_CMD --bootstrap-server $BOOTSTRAP --list > /dev/null 2>&1; do
  echo "  Kafka niedostępna, czekam 3s..."
  sleep 3
done

echo "Kafka gotowa, tworzę topiki..."

create_topic "competitor.data.raw"          3 604800000
create_topic "competitor.data.dlq"          1 2592000000
create_topic "competitor.insights.created"  3 604800000
create_topic "content.generation.requested" 3 86400000
create_topic "content.generation.completed" 3 86400000
create_topic "post.scheduled"               3 172800000
create_topic "post.published"               3 604800000

echo ""
echo "Topiki:"
$TOPICS_CMD --bootstrap-server $BOOTSTRAP --list