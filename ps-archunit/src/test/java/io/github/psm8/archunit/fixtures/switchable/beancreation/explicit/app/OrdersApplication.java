package io.github.psm8.archunit.fixtures.switchable.beancreation.explicit.app;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;

@SpringBootConfiguration
public class OrdersApplication {
	@Bean
	OrderService orderService() {
		return new OrderService();
	}
}
