package com.github.senocak.skj

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jobrunr.configuration.JobRunr
import org.jobrunr.dashboard.JobRunrDashboardWebServerConfiguration
import org.jobrunr.jobs.mappers.JobMapper
import org.jobrunr.scheduling.JobScheduler
import org.jobrunr.server.BackgroundJobServerConfiguration
import org.jobrunr.storage.StorageProvider
import org.jobrunr.storage.StorageProviderUtils.DatabaseOptions
import org.jobrunr.storage.sql.postgres.PostgresStorageProvider
import org.jobrunr.utils.mapper.jackson.JacksonJsonMapper
import org.springframework.boot.autoconfigure.flyway.FlywayProperties
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.stereotype.Component
import java.time.Duration
import javax.sql.DataSource

@Component
class DataSourceConfig(
    private val datasource: DataSourceConfigs,
    private val hikari: HikariProperties,
    private val flyway: FlywayConfig,
    private val jobRunrProperties: JobRunrProperties
){
    @Bean
    @Primary
    fun dataSource(): DataSource =
        when {
            datasource.url!!.contains(other = "jdbc:postgresql") -> DriverManagerDataSource()
                .also { db: DriverManagerDataSource ->
                    db.url = datasource.url
                    db.username = datasource.username
                    db.password = datasource.password
                }
            else -> throw RuntimeException("Not configured")
        }

    @Bean
    fun hikariDataSource(dataSource: DataSource): HikariDataSource =
        HikariDataSource(HikariConfig()
            .also { it: HikariConfig ->
                it.dataSource = dataSource
                it.poolName = hikari.poolName ?: "SpringKotlinJPAHikariCP"
                it.minimumIdle = hikari.minimumIdle
                it.maximumPoolSize = hikari.maximumPoolSize
                it.maxLifetime = hikari.maxLifetime
                it.idleTimeout = hikari.idleTimeout
                it.connectionTimeout = hikari.connectionTimeout
                it.transactionIsolation = hikari.transactionIsolation ?: "TRANSACTION_READ_COMMITTED"
            }
        )

    @Bean(initMethod = "migrate")
    fun flyway(dataSource: DataSource): Flyway =
        configureFlyway(dataSource = dataSource, flyway = flyway)

    @Bean
    fun storageProvider(dataSource: DataSource): StorageProvider {
        val storageProvider = PostgresStorageProvider(dataSource, DatabaseOptions.CREATE)
        // val storageProvider = InMemoryStorageProvider()
        storageProvider.setJobMapper(JobMapper(JacksonJsonMapper()))
        return storageProvider
    }

    @Bean
    fun scheduler(storageProvider: StorageProvider): JobScheduler = JobScheduler(storageProvider)

    @Bean
    fun jobScheduler(storageProvider: StorageProvider): JobScheduler {
        val usingStandardDashboardConfiguration: JobRunrDashboardWebServerConfiguration = JobRunrDashboardWebServerConfiguration.usingStandardDashboardConfiguration()
        usingStandardDashboardConfiguration.andPort(jobRunrProperties.dashboard.port.toInt())
        usingStandardDashboardConfiguration.andBasicAuthentication("anil", "senocak")
        val backgroundJobServerConfiguration: BackgroundJobServerConfiguration = BackgroundJobServerConfiguration.usingStandardBackgroundJobServerConfiguration()
        backgroundJobServerConfiguration.andName(jobRunrProperties.backgroundJobServer.name)
        backgroundJobServerConfiguration.andWorkerCount(jobRunrProperties.backgroundJobServer.workerCount.toInt())
        backgroundJobServerConfiguration.andScheduledJobsRequestSize(jobRunrProperties.backgroundJobServer.scheduledJobsRequestSize.toInt())
        backgroundJobServerConfiguration.andOrphanedJobsRequestSize(jobRunrProperties.backgroundJobServer.orphanedJobsRequestSize.toInt())
        backgroundJobServerConfiguration.andSucceededJobsRequestSize(jobRunrProperties.backgroundJobServer.succeededJobsRequestSize.toInt())
        backgroundJobServerConfiguration.andInterruptJobsAwaitDurationOnStopBackgroundJobServer(Duration.parse(jobRunrProperties.backgroundJobServer.interruptJobsAwaitDurationOnStop))
        return JobRunr.configure()
            .useStorageProvider(storageProvider)
            .useBackgroundJobServer()
            .useDashboardIf(jobRunrProperties.dashboard.enabled.toBoolean(), usingStandardDashboardConfiguration)
            .useBackgroundJobServerIf(jobRunrProperties.backgroundJobServer.enabled.toBoolean(), backgroundJobServerConfiguration)
            .initialize()
            .jobScheduler
        //or return JobScheduler(storageProvider)
    }
}

@ConfigurationProperties(prefix = "spring.datasource")
class DataSourceConfigs: DataSourceProperties()

@ConfigurationProperties(prefix = "spring.datasource.hikari")
class HikariProperties: HikariConfig()

@ConfigurationProperties(prefix = "spring.flyway")
@Primary
class FlywayConfig: FlywayProperties()

fun configureFlyway(dataSource: DataSource, flyway: FlywayConfig): Flyway =
    Flyway.configure()
        .dataSource(dataSource)
        .failOnMissingLocations(flyway.isFailOnMissingLocations)
        .locations(flyway.locations.joinToString(separator = ","))
        .defaultSchema(flyway.defaultSchema)
        .table(flyway.table)
        .sqlMigrationPrefix(flyway.sqlMigrationPrefix)
        .sqlMigrationSeparator(flyway.sqlMigrationSeparator)
        .load()

@ConfigurationProperties(prefix = "org.jobrunr")
class JobRunrProperties{
    lateinit var dashboard: JobRunrDashboardProperties
    lateinit var backgroundJobServer: JobRunrBackgroundJobServerProperties
}

class JobRunrDashboardProperties {
    lateinit var enabled: String
    lateinit var port: String
}

class JobRunrBackgroundJobServerProperties {
    lateinit var name: String
    lateinit var enabled: String
    lateinit var workerCount: String
    lateinit var scheduledJobsRequestSize: String
    lateinit var orphanedJobsRequestSize: String
    lateinit var succeededJobsRequestSize: String
    lateinit var interruptJobsAwaitDurationOnStop: String
}