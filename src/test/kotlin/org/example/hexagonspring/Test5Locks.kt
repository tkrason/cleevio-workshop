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
class Test5AsyncWithLocks(
    @Autowired private val test5Service: Test5Service,
) {

    private val logger = LoggerFactory.getLogger(Test1TransactionalBlockingConnectionPool::class.java)

    /**
     *  Prerequisites: Check connection pool is 2
     */
    @Test
    fun `test when are locks released`() {
        logger.info("Is some tx active: ${TransactionSynchronizationManager.isActualTransactionActive()}")
        // connection pool -> 2 free

        test5Service.doWork(number = 1) // connection pool -> 1 free

        // this will BLOCK the connection whole time waiting for lock!
        test5Service.doWork(number = 2) // connection pool -> 0 free

        Thread.sleep(100)

        // these can start executing only when test5Service.doWork(number = 1) finishes!
        test5Service.doUnrelatedTransactionalWork(number = 3)
        test5Service.doUnrelatedTransactionalWork(number = 4)
        test5Service.doUnrelatedTransactionalWork(number = 5)

        logger.info("Invocations done")
        Thread.sleep(10000)
    }
}

@Suppress("LoggingSimilarMessage")
@Service
class Test5Service(@Autowired private val test5AnotherService: Test5AnotherService) {

    private val logger = LoggerFactory.getLogger(Test3Service::class.java)

    @Async
    @Transactional
    fun doWork(number: Int) {
        logger.info("Number $number is starting... Tx active: ${TransactionSynchronizationManager.isActualTransactionActive()}")

        test5AnotherService.doWorkInTransactionWithLock(number)
        test5AnotherService.doLoooooooooooongUnrelatedWork(number)
        test5AnotherService.doQuickWorkWithLock(number)

        logger.info("Number $number done!")
    }

    @Transactional
    @Async
    fun doUnrelatedTransactionalWork(number: Int) {
        logger.info("Number $number starting unrelated work!")
        Thread.sleep(100)
        logger.info("Number $number done unrelated work")
    }

}

@Suppress("LoggingSimilarMessage")
@Service
class Test5AnotherService {

    private val logger = LoggerFactory.getLogger(Test3Service::class.java)

    @Transactional
    @Lock(module = "test", lockName = "test")
    fun doWorkInTransactionWithLock(number: Int) {
        logger.info("Number $number is starting first work with lock")
        Thread.sleep(1000)
        logger.info("Number $number done first work with lock!")
    }

    fun doLoooooooooooongUnrelatedWork(number: Int) {
        logger.info("Number $number is starting looooooong work")
        Thread.sleep(5000)
        logger.info("Number $number done with looooooong work!")
    }

    @Transactional
    @Lock(module = "test", lockName = "test")
    fun doQuickWorkWithLock(number: Int) {
        logger.info("Number $number is starting quick work")
        Thread.sleep(100)
        logger.info("Number $number done quick work")
    }

}
