package com.noom.interview.fullstack.sleep

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
@Transactional
abstract class IntegrationTest
