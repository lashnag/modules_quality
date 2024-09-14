package ru.lashnev.modules.quality.controllers.web

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.lashnev.modules.quality.model.Warning
import ru.lashnev.modules.quality.service.WarningsCompositeService
import ru.lashnev.modules.quality.utils.logger

@RestController
@RequestMapping("/warnings")
class ModulesQualityService(private val compositeService: WarningsCompositeService) {
    @GetMapping("/{module-name}/all")
    fun getWarnings(@PathVariable("module-name") moduleName: String): List<Warning> {
        return compositeService.getWarnings(moduleName).also {
            logger.info("Request getWarnings module name $moduleName return errors $it")
        }
    }

    companion object {
        private val logger = logger()
    }
}
