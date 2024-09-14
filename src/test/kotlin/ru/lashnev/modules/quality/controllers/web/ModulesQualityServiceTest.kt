package ru.lashnev.modules.quality.controllers.web

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.stub
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.ErrorResponse
import ru.lashnev.modules.quality.model.Warning
import ru.lashnev.modules.quality.model.WarningType
import ru.lashnev.modules.quality.service.WarningsCompositeService

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ModulesQualityServiceTest {
    @Autowired
    lateinit var testRestTemplate: TestRestTemplate

    @MockBean
    lateinit var compositeService: WarningsCompositeService

    @Test
    fun returnServerErrorIfSomeException() {
        compositeService.stub {
            on { getWarnings(any()) } doThrow IllegalStateException()
        }

        val response = testRestTemplate.exchange(
            "/warnings/service-name/all",
            HttpMethod.GET,
            null,
            Any::class.java
        )

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
    }

    @Test
    fun returnOkIfNoWarnings() {
        compositeService.stub {
            on { getWarnings(any()) } doReturn emptyList()
        }

        val response = testRestTemplate.exchange(
            "/warnings/service-name/all",
            HttpMethod.GET,
            null,
            Array<Warning>::class.java
        )

        assertEquals(HttpStatus.OK, response.statusCode)
    }

    @Test
    fun returnListIfThereIsWarnings() {
        compositeService.stub {
            on { getWarnings(any()) } doReturn listOf(
                Warning(WarningType.ACYCLIC_DEPENDENCY_VIOLATION, "", ""),
                Warning(WarningType.STABLE_ABSTRACTION_VIOLATION, "", "")
            )
        }

        val response = testRestTemplate.exchange(
            "/warnings/service-name/all",
            HttpMethod.GET,
            null,
            Array<Warning>::class.java
        )

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(2, response.body!!.size)
    }
}
