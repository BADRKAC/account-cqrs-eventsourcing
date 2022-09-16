package ma.insea.comptecqrses.commands.aggregates;

import ma.insea.comptecqrses.commonapi.commands.CreateAccountCommand;
import ma.insea.comptecqrses.commonapi.commands.CreditAccountCommand;
import ma.insea.comptecqrses.commonapi.commands.WithdrawalAccountCommand;
import ma.insea.comptecqrses.commonapi.enums.AccountStatus;
import ma.insea.comptecqrses.commonapi.events.AccountActivatedEvent;
import ma.insea.comptecqrses.commonapi.events.AccountCreatedEvent;
import ma.insea.comptecqrses.commonapi.events.AccountCreditedEvent;
import ma.insea.comptecqrses.commonapi.events.AccountWithdrawnEvent;
import ma.insea.comptecqrses.commonapi.exceptions.CannotCreateAccountException;
import ma.insea.comptecqrses.commonapi.exceptions.InsufficientBalanceException;
import ma.insea.comptecqrses.commonapi.exceptions.NegativeAmountException;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

@Aggregate
public class AccountAggregate {
    @AggregateIdentifier
    private String accountId;
    private double balance;
    private String currency;
    private AccountStatus status;

    public AccountAggregate() {
    }

    @CommandHandler
    public AccountAggregate(CreateAccountCommand createAccountCommand) {
        if(createAccountCommand.getInitialBalance()<0){
            throw new CannotCreateAccountException("Cannot create account ! negative balance");
        }
        AggregateLifecycle.apply(new AccountCreatedEvent(createAccountCommand.getId(),
                createAccountCommand.getInitialBalance(),
                createAccountCommand.getCurrency()));
    }

    @CommandHandler
    public void handle(CreditAccountCommand creditAccountCommand){
        if(creditAccountCommand.getAmount()<0){
            throw new NegativeAmountException("Cannot credit account ! negative amount ");
        }
        AggregateLifecycle.apply(new AccountCreditedEvent(
                creditAccountCommand.getId(),
                creditAccountCommand.getAmount(),
                creditAccountCommand.getCurrency()
        ));
    }

    @CommandHandler
    public void handle(WithdrawalAccountCommand withdrawalAccountCommand){
        if(withdrawalAccountCommand.getAmount() > this.balance){
            throw new InsufficientBalanceException("Insufficient balance ! Your balance is :"+balance);
        }
        AggregateLifecycle.apply(new AccountWithdrawnEvent(
                withdrawalAccountCommand.getId(),
                withdrawalAccountCommand.getAmount(),
                withdrawalAccountCommand.getCurrency()
        ));
    }

    @EventSourcingHandler
    public void on(AccountCreatedEvent event){
        this.accountId=event.getId();
        this.balance=event.getInitialBalance();
        this.currency=event.getCurrency();
        this.status=AccountStatus.CREATED;
        AggregateLifecycle.apply(new AccountActivatedEvent(event.getId(),AccountStatus.ACTIVATED));
    }

    @EventSourcingHandler
    public void on(AccountActivatedEvent event){
        this.status=event.getStatus();
}

    @EventSourcingHandler
    public void on(AccountCreditedEvent event){
        this.balance+=event.getAmount();
    }

    @EventSourcingHandler
    public void on(AccountWithdrawnEvent event){
        this.balance-=event.getAmount();
    }

}
