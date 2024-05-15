package org.example.hexagonspring

import com.cleevio.library.lockinghandler.service.Lock
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
class Test4AsyncWithLocks(
    @Autowired private val test4Service: Test4Service,
    @Autowired private val test4AnotherService: Test4AnotherService,
) {

    private val logger = LoggerFactory.getLogger(Test1TransactionalBlockingConnectionPool::class.java)

    /**
     *  Prerequisites: Check connection pool is 10
     */
    @Test
    fun `test call three async functions with same lock - no transactional - connection pool size 10 - check docker after`() {
        logger.info("Is some tx active: ${TransactionSynchronizationManager.isActualTransactionActive()}")

        test4Service.doWorkAsyncWithLock(number = 1)
        test4Service.doWorkAsyncWithLock(number = 2)
        test4Service.doWorkAsyncWithLock(number = 3)

        logger.info("Invocations done")
        Thread.sleep(3500)
    }

    /**
     *  Prerequisites: Check connection pool is 10
     */
    @Test
    fun `test call three async functions with same lock - transactional - connection pool size 10 - check docker after`() {
        logger.info("Is some tx active: ${TransactionSynchronizationManager.isActualTransactionActive()}")

        test4Service.doWorkAsyncWithLockAndTx(number = 1)
        test4Service.doWorkAsyncWithLockAndTx(number = 2)
        test4Service.doWorkAsyncWithLockAndTx(number = 3)

        logger.info("Invocations done")
        Thread.sleep(3500)
    }

    /**
     *  Prerequisites: Check connection pool is 1
     */
    @Test
    fun `test call three async functions with same lock - transactional - connection pool size 1 - check docker after`() {
        logger.info("Is some tx active: ${TransactionSynchronizationManager.isActualTransactionActive()}")

        test4Service.doWorkAsyncWithLockAndTx(number = 1)
        test4Service.doWorkAsyncWithLockAndTx(number = 2)
        test4Service.doWorkAsyncWithLockAndTx(number = 3)

        logger.info("Invocations done")
        Thread.sleep(3500)
    }

    @Test
    fun `test call three async functions with same lock - transactional - connection pool size 4 - unrelated transactions after are starved - check docker after`() {
        logger.info("Is some tx active: ${TransactionSynchronizationManager.isActualTransactionActive()}")

        // Three requests came, each has its own transaction
        test4Service.doWorkAsyncWithLockAndTxLoooooooooong(number = 1)
        test4Service.doWorkAsyncWithLockAndTxLoooooooooong(number = 2)
        test4Service.doWorkAsyncWithLockAndTxLoooooooooong(number = 3)

        logger.info("Long invocation done")
        Thread.sleep(250)
        // at this point, three connections are already taken
        // one is actually "working" in db
        // other two are waiting to obtain lock and BLOCKING the connection for that duration
        test4AnotherService.doWorkTransactional(number = 4)
        test4AnotherService.doWorkTransactional(number = 5)
        test4AnotherService.doWorkTransactional(number = 6)
        test4AnotherService.doWorkTransactional(number = 7)

        logger.info("Invocations done")
        Thread.sleep(10000)
    }
}

@Suppress("LoggingSimilarMessage")
@Service
class Test4Service {

    private val logger = LoggerFactory.getLogger(Test3Service::class.java)

    @Async
    @Lock(module = "test", lockName = "test")
    fun doWorkAsyncWithLock(number: Int) {
        logger.info("Number $number is starting... Tx active: ${TransactionSynchronizationManager.isActualTransactionActive()}")
        Thread.sleep(1000)
        logger.info("Number $number done!")
    }

    @Async
    @Lock(module = "test", lockName = "test")
    @Transactional
    fun doWorkAsyncWithLockAndTx(number: Int) {
        logger.info("Number $number is starting... Tx active: ${TransactionSynchronizationManager.isActualTransactionActive()}")
        Thread.sleep(1000)
        logger.info("Number $number done!")
    }

    @Async
    @Lock(module = "test", lockName = "test")
    @Transactional
    fun doWorkAsyncWithLockAndTxLoooooooooong(number: Int) {
        logger.info("Number $number is starting... Tx active: ${TransactionSynchronizationManager.isActualTransactionActive()}")
        Thread.sleep(10000)
        logger.info("Number $number done!")
    }

}

@Suppress("LoggingSimilarMessage")
@Service
class Test4AnotherService {

    private val logger = LoggerFactory.getLogger(Test3Service::class.java)

    @Transactional
    fun doWorkTransactional(number: Int) {
        logger.info("Number $number is starting... Tx active: ${TransactionSynchronizationManager.isActualTransactionActive()}")
        Thread.sleep(1000)
        logger.info("Number $number done!")
    }
}
