package dev.codescreen.bankledger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

@SpringBootApplication
@EnableScheduling
public class BankLedgerSystem {
    public static void main(String[] args){
        SpringApplication.run(BankLedgerSystem.class, args);
        System.out.print("Server Running");
    }
}
