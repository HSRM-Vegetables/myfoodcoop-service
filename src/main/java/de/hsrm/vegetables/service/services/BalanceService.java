package de.hsrm.vegetables.service.services;

import de.hsrm.vegetables.Stadtgemuese_Backend.model.BalanceChangeType;
import de.hsrm.vegetables.service.domain.dto.BalanceDto;
import de.hsrm.vegetables.service.domain.dto.BalanceHistoryItemDto;
import de.hsrm.vegetables.service.exception.ErrorCode;
import de.hsrm.vegetables.service.exception.errors.NameInUseError;
import de.hsrm.vegetables.service.exception.errors.TooManyResultsError;
import de.hsrm.vegetables.service.exception.errors.http.NotFoundError;
import de.hsrm.vegetables.service.repositories.BalanceHistoryItemRepository;
import de.hsrm.vegetables.service.repositories.BalanceRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired})) // Does magic to autowire all @NonNull fields
public class BalanceService {

    @NonNull
    private final BalanceRepository balanceRepository;

    @NonNull
    private final BalanceHistoryItemRepository balanceHistoryItemRepository;

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

        saveBalanceChange(balance, BalanceChangeType.WITHDRAW, amount);

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

        saveBalanceChange(balance, BalanceChangeType.WITHDRAW, amount);

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

        saveBalanceChange(balance, BalanceChangeType.TOPUP, amount);

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

        BalanceDto balanceDto = balanceRepository.save(balance);

        saveBalanceChange(balance, BalanceChangeType.SET, amount);

        return balanceDto;
    }

    /**
     * Find balance history items for a user within the specified date range
     *
     * @param balanceDto The balance of the user who created the balance history items
     * @param fromDate Start of time window of the balance history item list
     * @param toDate End of time window of the balance history item list
     * @return A list of balance history items created by the given user
     */
    public List<BalanceHistoryItemDto> findAllByBalanceDtoAndCreatedOnBetween(
            BalanceDto balanceDto, OffsetDateTime fromDate, OffsetDateTime toDate, Pageable pageable) {

        return balanceHistoryItemRepository.findAllByBalanceDtoAndCreatedOnBetween(
                balanceDto, fromDate, toDate, pageable);
    }

    /**
     * Create and save a balance history item
     *
     * @param balanceDto The changed balance the balance history item refers to
     * @param balanceChangeType The type of balance change (TOPUP, WITHDRAW, etc.)
     * @param amount The amount the balance was changed by or changed to
     */
    private void saveBalanceChange(BalanceDto balanceDto, BalanceChangeType balanceChangeType, float amount) {
        BalanceHistoryItemDto balanceHistoryItem = new BalanceHistoryItemDto();

        balanceHistoryItem.setBalanceDto(balanceDto);
        balanceHistoryItem.setBalanceChangeType(balanceChangeType);
        balanceHistoryItem.setAmount(amount);

        balanceHistoryItemRepository.save(balanceHistoryItem);
    }
}
