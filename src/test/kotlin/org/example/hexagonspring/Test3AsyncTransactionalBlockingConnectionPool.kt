package org.example.hexagonspring

import jakarta.transaction.Transactional
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionSynchronizationManager

@Suppress("LoggingSimilarMessage")
@SpringBootTest
class Test3TransactionalBlockingConnectionPool(@Autowired private val test3Service: Test3Service) {

    private val logger = LoggerFactory.getLogger(Test1TransactionalBlockingConnectionPool::class.java)

    /**
     *  Prerequisites: ! Check connection pool is 10 !
     */
    @Test
    fun `test three concurrent async transactions - connection pool size 10`() {
        logger.info("Is some tx active: ${TransactionSynchronizationManager.isActualTransactionActive()}")

        test3Service.doWorkTransactionalAndAsync(number = 1)
        test3Service.doWorkTransactionalAndAsync(number = 2)
        test3Service.doWorkTransactionalAndAsync(number = 3)

        logger.info("Invocations done")
        Thread.sleep(1500)
    }

    /**
     *  Prerequisites: Check connection pool is 1
     */
    @Test
    fun `test three concurrent async transactions - connection pool size 1`() {
        logger.info("Is some tx active: ${TransactionSynchronizationManager.isActualTransactionActive()}")

        test3Service.doWorkTransactionalAndAsync(number = 1)
        test3Service.doWorkTransactionalAndAsync(number = 2)
        test3Service.doWorkTransactionalAndAsync(number = 3)

        logger.info("Invocations done")
        Thread.sleep(3500)
    }

    /**
     *  Prerequisites: Check connection pool is 1
     */
    @Test
    fun `test calling two async transactions - inside call different service with transactional - connection pool size 1`() {
        logger.info("Is some tx active: ${TransactionSynchronizationManager.isActualTransactionActive()}")

        // note: these are NOT guaranteed to execute in this order!
        test3Service.doWorkTransactionalAndAsyncAndCallTx(number = 1)
        test3Service.doWorkTransactionalAndAsyncAndCallTx(number = 2)

        logger.info("Invocations done")
        Thread.sleep(5000)
    }

    /**
     *  Prerequisites: Check connection pool is 1
     */
    @Test
    fun `test calling two async transactions - inside call different service with another ASYNC transactional - connection pool size 1`() {
        logger.info("Is some tx active: ${TransactionSynchronizationManager.isActualTransactionActive()}")

        test3Service.doWorkTransactionalAndAsyncAndCallTxAsync(number = 1)
        test3Service.doWorkTransactionalAndAsyncAndCallTxAsync(number = 2)

        logger.info("Invocations done")
        Thread.sleep(5000)
    }

    /**
     *  Prerequisites: Check connection pool is 1
     */
    @Test
    fun `test calling two async transactions - inside call different service with another transactional REQUIRES_NEW - connection pool size 1`() {
        logger.info("Is some tx active: ${TransactionSynchronizationManager.isActualTransactionActive()}")

        test3Service.doWorkTransactionalAndAsyncAndCallTxRequiresNew(number = 1)
        test3Service.doWorkTransactionalAndAsyncAndCallTxRequiresNew(number = 2)

        logger.info("Invocations done")
        Thread.sleep(5000)
    }
}

@Suppress("LoggingSimilarMessage")
@Service
class Test3Service(
    private val test2AnotherService: Test2AnotherService,
) {

    private val logger = LoggerFactory.getLogger(Test3Service::class.java)

    @Async
    @Transactional
    fun doWorkTransactionalAndAsync(number: Int) {
        logger.info("Number $number is starting... Tx active: ${TransactionSynchronizationManager.isActualTransactionActive()}")
        Thread.sleep(1000)
        logger.info("Number $number done!")
    }

    @Async
    @Transactional
    fun doWorkTransactionalAndAsyncAndCallTx(number: Int) {
        logger.info("Number $number is starting... Tx active: ${TransactionSynchronizationManager.isActualTransactionActive()}")
        Thread.sleep(1000)
        test2AnotherService.anotherTransaction(number)
        logger.info("Number $number done!")
    }

    @Async
    @Transactional
    fun doWorkTransactionalAndAsyncAndCallTxAsync(number: Int) {
        logger.info("Number $number is starting... Tx active: ${TransactionSynchronizationManager.isActualTransactionActive()}")
        Thread.sleep(1000)
        test2AnotherService.anotherAsyncTransaction(number)
        logger.info("Number $number done!")
    }

    @Async
    @Transactional
    fun doWorkTransactionalAndAsyncAndCallTxRequiresNew(number: Int) {
        logger.info("Number $number is starting... Tx active: ${TransactionSynchronizationManager.isActualTransactionActive()}")
        Thread.sleep(1000)
        test2AnotherService.anotherTransactionWithRequiresNewTx(number)
        logger.info("Number $number done!")
    }
}

@Suppress("LoggingSimilarMessage")
@Service
class Test2AnotherService {

    private val logger = LoggerFactory.getLogger(Test2AnotherService::class.java)

    @Transactional
    fun anotherTransaction(number: Int) {
        logger.info("Inside new transaction: $number!")
        Thread.sleep(1000)
        logger.info("Exiting new transaction: $number")
    }

    @Async
    @Transactional
    fun anotherAsyncTransaction(number: Int) {
        logger.info("Inside new async transaction! $number")
        Thread.sleep(1000)
        logger.info("Exiting new async transaction $number")
    }

    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    fun anotherTransactionWithRequiresNewTx(number: Int) {
        logger.info("Inside REQUIRES_NEW transaction!")
        Thread.sleep(1000)
        logger.info("exiting REQUIRES_NEW transaction")
    }

}