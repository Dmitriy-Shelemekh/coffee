package com.example.coffee.controller

import com.example.coffee.model.dto.CoffeeDto
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.jdbc.Sql
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.PostgreSQLContainer
import java.util.UUID

@Transactional
@SpringBootTest
@Sql("/sql/insert_into-coffee.sql")
@TestMethodOrder(OrderAnnotation::class)
class CoffeeControllerTest(
    @Autowired val coffeeController: CoffeeController
) {
    companion object {
        private val db = PostgreSQLContainer("postgres:15")
            .withDatabaseName("test")
            .withClasspathResourceMapping("/sql/create_table-coffee.sql", "/docker-entrypoint-initdb.d/", BindMode.READ_ONLY)

        @BeforeAll
        @JvmStatic
        fun startDBContainer() {
            db.start()
        }

        @AfterAll
        @JvmStatic
        fun stopDBContainer() {
            db.stop()
        }

        @DynamicPropertySource
        @JvmStatic
        fun registerDBContainer(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", db::getJdbcUrl)
            registry.add("spring.datasource.username", db::getUsername)
            registry.add("spring.datasource.password", db::getPassword)
        }
    }

    @Test
    @Order(1)
    fun `dbContainer is running`() {
        Assertions.assertTrue(db.isRunning)
    }

    @Test
    @Order(2)
    fun `postCoffee is works`() = runBlocking {
        Assertions.assertTrue(coffeeController.getAllCoffee().body?.size == 1)
        val coffee = coffeeController.postCoffee(CoffeeDto("Ethiopia"))
        val resp = coffeeController.getCoffee(coffee.body!!.id)
        Assertions.assertTrue(resp.body?.name == "Ethiopia")
        Assertions.assertTrue(coffeeController.getAllCoffee().body?.size == 2)
    }

    @Test
    @Order(3)
    fun `getCoffee is works`() = runBlocking {
        val resp = coffeeController.getCoffee(UUID.fromString("9ba5f1b2-fc3c-42aa-b4dd-d2ba1f1b4da5"))
        Assertions.assertTrue(resp.body?.name == "Ethiopia")
        Assertions.assertTrue(coffeeController.getAllCoffee().body?.size == 1)
    }

    @Test
    @Order(4)
    fun `deleteCoffee is works`() = runBlocking {
        val resp = coffeeController.deleteCoffee(UUID.fromString("9ba5f1b2-fc3c-42aa-b4dd-d2ba1f1b4da5"))
        Assertions.assertTrue(resp.statusCode == HttpStatus.OK)
    }

    @Test
    @Order(5)
    fun `getAllCoffee is works`() = runBlocking {
        val resp = coffeeController.getAllCoffee()
        Assertions.assertTrue(resp.body?.isNotEmpty() ?: false)
    }
}