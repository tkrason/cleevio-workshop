package org.example.hexagonspring

import jakarta.transaction.Transactional
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionSynchronizationManager


/**
 *  Hikari CP - connection pool
 *
 *  What is connection in PostgreSQL?
 *   * TCP connection through which data (queries) can be sent to server.
 *   * Normally without Spring we would open new connection for example for each request.
 *   * TCP connection setup every time -> expensive
 *   * Why not reuse them? -> HikariCP
 *
 *  HikariCP
 *   * pool of 10 connections (default) given through DataSource.getConnection()
 */
@Suppress("LoggingSimilarMessage")
@SpringBootTest
class Test1TransactionalBlockingConnectionPool(@Autowired private val test1Service: Test1Service) {

    private val logger = LoggerFactory.getLogger(Test1TransactionalBlockingConnectionPool::class.java)

    /**
     *  Prerequisites: Check connection pool is 10
     */
    @Test
    fun `test transactional blocking connection pool - size 10`() {
        logger.info("Is some tx active: ${TransactionSynchronizationManager.isActualTransactionActive()}")

        test1Service.transactional(number = 1)
        test1Service.transactional(number = 2)
        test1Service.transactional(number = 3)

        logger.info("Test done")
    }

    /**
     *  Prerequisites: Check connection pool is 1
     */
    @Test
    fun `test transactional blocking connection pool - size 1`() {
        logger.info("Is some tx active: ${TransactionSynchronizationManager.isActualTransactionActive()}")

        test1Service.transactional(number = 1)
        test1Service.transactional(number = 2)
        test1Service.transactional(number = 3)

        logger.info("Test done")
    }
}

@Service
class Test1Service {

    private val logger = LoggerFactory.getLogger(Test1Service::class.java)

    @Transactional
    fun transactional(number: Int) {
        logger.info("Number $number is starting... Tx active: ${TransactionSynchronizationManager.isActualTransactionActive()}")
        Thread.sleep(1000)
        logger.info("Number $number done!")
    }
}
