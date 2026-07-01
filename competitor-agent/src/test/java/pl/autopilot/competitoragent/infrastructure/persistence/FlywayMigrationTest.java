package pl.autopilot.competitoragent.infrastructure.persistence;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenCode;

@Testcontainers
class FlywayMigrationTest {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer(
        DockerImageName.parse("pgvector/pgvector:pg16")
                .asCompatibleSubstituteFor("postgres"))
        .withDatabaseName("marketing_autopilot")
        .withUsername("app")
        .withPassword("app")
        .withInitScript("db/init.sql");

    @Test
    void shouldApplyAllMigrationsWithoutErrors() {
        // when / then — migracje nie rzucają wyjątku
        thenCode(() -> flyway().migrate()).doesNotThrowAnyException();
    }

    @Test
    void shouldCreateAllExpectedTables() throws Exception {
        // given
        flyway().migrate();

        // when
        DataSource ds = dataSource();
        try (Connection conn = ds.getConnection();
             ResultSet rs = conn.getMetaData().getTables(
                     null, "competitor", null, new String[]{"TABLE"})) {

            java.util.List<String> tables = new java.util.ArrayList<>();
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME"));
            }

            // then
            then(tables).contains(
                    "competitor_posts",
                    "competitor_profiles",
                    "engagement_analyses",
                    "hashtag_performances",
                    "analysis_results"
            );
        }
    }

    @Test
    void shouldCreateEmbeddingColumnWithVectorType() throws Exception {
        // given
        flyway().migrate();

        // when
        DataSource ds = dataSource();
        try (Connection conn = ds.getConnection();
             ResultSet rs = conn.getMetaData().getColumns(
                     null, "competitor", "competitor_posts", "embedding")) {

            // then
            then(rs.next()).isTrue();
            then(rs.getString("TYPE_NAME")).isEqualTo("vector");
        }
    }

    @Test
    void shouldCreateIndexesOnCompetitorPosts() throws Exception {
        // given
        flyway().migrate();

        // when
        DataSource ds = dataSource();
        try (Connection conn = ds.getConnection();
             ResultSet rs = conn.createStatement().executeQuery("""
                     SELECT indexname
                     FROM pg_indexes
                     WHERE schemaname = 'competitor'
                       AND tablename  = 'competitor_posts'
                     """)) {

            java.util.List<String> indexes = new java.util.ArrayList<>();
            while (rs.next()) {
                indexes.add(rs.getString("indexname"));
            }

            // then
            then(indexes).contains(
                    "idx_competitor_posts_username",
                    "idx_competitor_posts_published_at",
                    "idx_competitor_posts_embedding"
            );
        }
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Flyway flyway() {
        return Flyway.configure()
                .dataSource(dataSource())
                .locations("classpath:db/migration")
                .defaultSchema("competitor")
                .schemas("competitor")
                .load();
    }

    private DataSource dataSource() {
        org.springframework.jdbc.datasource.DriverManagerDataSource ds =
                new org.springframework.jdbc.datasource.DriverManagerDataSource();
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setUrl(postgres.getJdbcUrl());
        ds.setUsername(postgres.getUsername());
        ds.setPassword(postgres.getPassword());
        return ds;
    }
}