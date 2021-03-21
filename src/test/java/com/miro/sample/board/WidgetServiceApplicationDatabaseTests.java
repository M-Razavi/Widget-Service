package com.miro.sample.board;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("h2")
class WidgetServiceApplicationDatabaseTests {

    @Test
    void contextLoadsForInMemoryProfile() {
        Assertions.assertDoesNotThrow(this::doNotThrowException);
    }

    private void doNotThrowException() {
        //This method will never throw exception
    }

}
