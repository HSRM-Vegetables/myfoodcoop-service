package de.hsrm.vegetables.service.services;

import de.hsrm.vegetables.service.domain.dto.BalanceDto;
import de.hsrm.vegetables.service.exception.ErrorCode;
import de.hsrm.vegetables.service.exception.errors.NameInUseError;
import de.hsrm.vegetables.service.exception.errors.TooManyResultsError;
import de.hsrm.vegetables.service.exception.errors.http.NotFoundError;
import de.hsrm.vegetables.service.repositories.BalanceRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired})) // Does magic to autowire all @NonNull fields
public class BalanceService {

    @NonNull
    private final BalanceRepository balanceRepository;

    /**
     * Returns the balance for a given user identified by name
     * Throws an error if no balance was found for the given name
     *
     * @param name The name to get the balance for
     * @return The balance for the specified user
     */
    public BalanceDto getBalance(String name) {
        List<BalanceDto> balances = balanceRepository.findByName(name);

        if (balances.size() > 1) {
            throw new TooManyResultsError();
        }

        if (balances.isEmpty()) {
            throw new NotFoundError("The balance for the given name was not found", ErrorCode.NO_BALANCE_FOUND);
        }

        return balances.get(0);
    }

    /**
     * Creates a new empty entry in the database with a balance of 0f.
     * If the given name is already in use, an error is thrown
     *
     * @param name The name for the new balance
     * @return The balance DTO for the new user
     */
    public BalanceDto createEmptyBalance(String name) {
        List<BalanceDto> balances = balanceRepository.findByName(name);

        if (!balances.isEmpty()) {
            throw new NameInUseError();
        }

        BalanceDto balanceDto = new BalanceDto();
        balanceDto.setName(name);
        balanceDto.setAmount(0f);

        return balanceRepository.save(balanceDto);
    }

    /**
     * Reduces the amount of money a given user has.
     * <p>
     * Throws a NotFoundError if the user wasn't found
     *
     * @param name   The name of the user to reduce the balance for
     * @param amount The amount to subtract from the users balance
     * @return The updated balance object
     */
    public BalanceDto withdraw(String name, Float amount) {
        BalanceDto balance = getBalance(name);
        balance.setAmount(balance.getAmount() - amount);

        return balanceRepository.save(balance);
    }

    /**
     * Reduces the amount of money a given user has.
     * <p>
     * Throws a NotFoundError if the user wasn't found
     *
     * @param balance The balance of the user
     * @param amount  The amount to subtract from the balance
     * @return The updated balance
     */
    public BalanceDto withdraw(BalanceDto balance, Float amount) {
        balance.setAmount(balance.getAmount() - amount);

        return balanceRepository.save(balance);
    }

    /**
     * Increases the amount of money a given user has.
     * <p>
     * Throws a NotFoundError if the user wasn't found
     *
     * @param name   The name of the user to increase the balance for
     * @param amount The amount to add to the users balance
     * @return The updated balance object
     */
    public BalanceDto topup(String name, Float amount) {
        BalanceDto balance = getBalance(name);
        balance.setAmount(balance.getAmount() + amount);

        return balanceRepository.save(balance);
    }

    /**
     * Updates a users balance.
     * If no balance for the given name was found, a new entry in the database will be created, no error will be thrown
     *
     * @param name   The name to set the amount for
     * @param amount The amount to set
     * @return The created or updated balance DTO
     */
    public BalanceDto upsert(String name, Float amount) {
        BalanceDto balance = new BalanceDto();
        balance.setName(name);
        balance.setAmount(amount);

        return balanceRepository.save(balance);
    }

}
