package ma.insea.comptecqrses.query.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.insea.comptecqrses.commonapi.enums.AccountStatus;
import ma.insea.comptecqrses.commonapi.enums.OperationTypes;
import ma.insea.comptecqrses.commonapi.events.AccountActivatedEvent;
import ma.insea.comptecqrses.commonapi.events.AccountCreatedEvent;
import ma.insea.comptecqrses.commonapi.events.AccountCreditedEvent;
import ma.insea.comptecqrses.commonapi.events.AccountWithdrawnEvent;
import ma.insea.comptecqrses.query.entities.Account;
import ma.insea.comptecqrses.query.entities.Operation;
import ma.insea.comptecqrses.query.repositories.AccountRepository;
import ma.insea.comptecqrses.query.repositories.OperationRepository;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
@Transactional
public class AccountServiceHandler {

    private AccountRepository accountRepository;
    private OperationRepository operationRepository;

    @EventHandler
    public void on(AccountCreatedEvent event){
        log.info("**** Account creation event received ! *****");
        Account account = new Account();
        account.setId(event.getId());
        account.setBalance(event.getInitialBalance());
        account.setCurrency(event.getCurrency());
        account.setStatus(event.getStatus());

       accountRepository.save(account);
    }

    @EventHandler
    public void on(AccountActivatedEvent event){
        log.info("**** Account activation event received ! *****");
        Account account =accountRepository.findById(event.getId()).get();
        if(account!=null) {
            account.setStatus(event.getStatus());
            accountRepository.save(account);
        }
    }
    @EventHandler
    public void on(AccountCreditedEvent event){
        log.info("**** Account credit event received ! *****");
        Operation operation = new Operation();
        Account account =accountRepository.findById(event.getId()).get();
        if(account!=null) {
            /*Operation*/
            operation.setOperationDate(event.getCreditAt());
            operation.setOperationType(OperationTypes.CREDIT);
            operation.setAmount(event.getAmount());
            operation.setAccount(account);
            operationRepository.save(operation);

            account.setBalance(account.getBalance()+event.getAmount());
            accountRepository.save(account);
        }
    }

    @EventHandler
    public void on(AccountWithdrawnEvent event){
        log.info("**** Account withdraw event received ! *****");
        Operation operation = new Operation();
        Account account =accountRepository.findById(event.getId()).get();
        if(account!=null) {
            /*Operation*/
            operation.setOperationDate(event.getWithdrawnAt());
            operation.setOperationType(OperationTypes.WITHDRAWAL);
            operation.setAmount(event.getAmount());
            operation.setAccount(account);
            operationRepository.save(operation);

            account.setBalance(account.getBalance() - event.getAmount());
            accountRepository.save(account);
        }
    }
}
