package co.edu.unbosque.tercero;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

class MsTerceroApplicationTest {

    @Test
    void classShouldBeAnnotatedAsSpringBootApplication() {
        SpringBootApplication annotation = MsTerceroApplication.class.getAnnotation(SpringBootApplication.class);
        assertThat(annotation).isNotNull();
    }

    @Test
    void classShouldExposeStaticMainMethod() throws Exception {
        Method main = MsTerceroApplication.class.getDeclaredMethod("main", String[].class);
        assertThat(Modifier.isStatic(main.getModifiers())).isTrue();
    }

    @Test
    void mainMethodShouldInvokeSpringApplicationRun() {
        try (MockedStatic<SpringApplication> springApp = Mockito.mockStatic(SpringApplication.class)) {
            springApp.when(() -> SpringApplication.run(MsTerceroApplication.class, new String[]{}))
                    .thenReturn(null);

            assertThatCode(() -> MsTerceroApplication.main(new String[]{}))
                    .doesNotThrowAnyException();

            springApp.verify(() -> SpringApplication.run(MsTerceroApplication.class, new String[]{}));
        }
    }
}
