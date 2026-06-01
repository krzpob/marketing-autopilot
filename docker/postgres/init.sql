-- Uruchamia się tylko przy pierwszym starcie kontenera

-- Rozszerzenie pgvector (dostępne w obrazie pgvector/pgvector:pg16)
CREATE EXTENSION IF NOT EXISTS vector;

-- Rozszerzenia pomocnicze
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS pg_trgm;  -- wyszukiwanie pełnotekstowe w hashtagach

-- Schemat dla competitor-agent
CREATE SCHEMA IF NOT EXISTS competitor;

-- Schemat dla analytics
CREATE SCHEMA IF NOT EXISTS analytics;

-- Uprawnienia dla użytkownika aplikacji
GRANT ALL PRIVILEGES ON SCHEMA competitor TO app;
GRANT ALL PRIVILEGES ON SCHEMA analytics TO app;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA competitor TO app;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA competitor TO app;

-- Domyślne uprawnienia dla nowych tabel
ALTER DEFAULT PRIVILEGES IN SCHEMA competitor
  GRANT ALL ON TABLES TO app;
ALTER DEFAULT PRIVILEGES IN SCHEMA competitor
  GRANT ALL ON SEQUENCES TO app;