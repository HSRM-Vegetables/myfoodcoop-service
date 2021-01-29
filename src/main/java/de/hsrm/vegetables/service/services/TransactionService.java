package de.hsrm.vegetables.service.services;

import de.hsrm.vegetables.Stadtgemuese_Backend.model.CartItem;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.QuantitySoldItem;
import de.hsrm.vegetables.service.domain.dto.BalanceDto;
import de.hsrm.vegetables.service.domain.dto.TransactionDto;
import de.hsrm.vegetables.service.domain.dto.StockDto;
import de.hsrm.vegetables.service.exception.ErrorCode;
import de.hsrm.vegetables.service.exception.errors.http.InternalError;
import de.hsrm.vegetables.service.exception.errors.http.NotFoundError;
import de.hsrm.vegetables.service.repositories.TransactionRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class TransactionService {

    @NonNull
    private final TransactionRepository transactionRepository;

    /**
     * Find multiple transactions by name
     *
     * @param balanceDto The balance of the user who performed the transactions
     * @return A list of transactions performed by the given user
     */
    public List<TransactionDto> getTransactions(BalanceDto balanceDto) {
        return transactionRepository.findAllByBalanceDto(balanceDto);
    }

    /**
     * Find all transactions between fromDate and toDate
     *
     * @param fromDateConverted start of time window of the purchase list
     * @param toDateConverted end of time window of the purchase list
     * @return A list of transactions in the given time
     */
    public List<TransactionDto> findAllByCreatedOnBetween(OffsetDateTime fromDateConverted, OffsetDateTime toDateConverted){
        return transactionRepository.findAllByCreatedOnBetween(fromDateConverted, toDateConverted);
    }
}
