package ru.lashnev.modules.quality.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@ConditionalOnProperty(name = ["lashnev.cache.enable"], havingValue = "true", matchIfMissing = false)
@Configuration
class CacheConfig {
    @EnableCaching
    @Configuration
    class CaffeineManagerConfiguration {
        @Bean
        @Primary
        fun daoCacheManager(daoCacheConfigProperties: DaoCacheProperties): CacheManager {
            val caffeineCacheManager = CaffeineCacheManager("modules-info")
            caffeineCacheManager.setCaffeine(
                Caffeine
                    .newBuilder()
                    .expireAfterWrite(daoCacheConfigProperties.ttl)
                    .maximumSize(daoCacheConfigProperties.maximumElements)
                    .softValues()
            )
            return caffeineCacheManager
        }

        @Bean
        fun localRepositoriesCacheManager(localRepositoriesCacheProperties: LocalRepositoriesCacheProperties): CacheManager {
            val caffeineCacheManager = CaffeineCacheManager("local-repositories")
            caffeineCacheManager.setCaffeine(
                Caffeine
                    .newBuilder()
                    .expireAfterWrite(localRepositoriesCacheProperties.ttl)
                    .maximumSize(localRepositoriesCacheProperties.maximumElements)
                    .softValues()
            )
            return caffeineCacheManager
        }
    }
}
