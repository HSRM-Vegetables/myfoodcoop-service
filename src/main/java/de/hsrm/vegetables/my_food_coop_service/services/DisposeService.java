package de.hsrm.vegetables.my_food_coop_service.services;

import de.hsrm.vegetables.Stadtgemuese_Backend.model.CartItem;
import de.hsrm.vegetables.my_food_coop_service.repositories.DisposedRepository;
import de.hsrm.vegetables.my_food_coop_service.domain.dto.DisposedDto;
import de.hsrm.vegetables.my_food_coop_service.domain.dto.StockDto;
import de.hsrm.vegetables.my_food_coop_service.domain.dto.UserDto;
import de.hsrm.vegetables.my_food_coop_service.exception.ErrorCode;
import de.hsrm.vegetables.my_food_coop_service.exception.errors.http.InternalError;
import de.hsrm.vegetables.my_food_coop_service.repositories.DisposedRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class DisposeService {

    @NonNull
    private final DisposedRepository disposedRepository;


    /**
     * Purchase items
     *
     * @param stockItems The StockDto that were disposed
     * @param cartItems  The amounts of the items that were disposed
     * @return The completed dispose
     */
    public DisposedDto disposedItems(UserDto userDto, List<StockDto> stockItems, List<CartItem> cartItems, Float totalPrice, Float totalVat) {
        List<DisposedDto> disposedItem = cartItems
                .stream()
                .map(item -> {
                    Optional<StockDto> associatedStockDto = stockItems
                            .stream()
                            .filter(stockItem -> stockItem.getId()
                                    .equals(item.getId()))
                            .findFirst();

                    if (associatedStockDto.isEmpty()) {
                        throw new InternalError("No matching stock item was found in stockItems", ErrorCode.STOCK_DTO_NOT_FOUND);
                    }

                    DisposedDto disposedDto = new DisposedDto();
                    disposedDto.setAmount(item.getAmount());
                    disposedDto.setPricePerUnit(associatedStockDto.get()
                            .getPricePerUnit());
                    disposedDto.setStockDto(associatedStockDto.get());
                    disposedDto.setUnitType(associatedStockDto.get()
                            .getUnitType());
                    disposedDto.setVat(associatedStockDto.get()
                            .getVat());

                    return disposedDto;
                })
                .map(disposedRepository::save)
                .collect(Collectors.toList());

        DisposedDto disposedDto = new DisposedDto();
        disposedDto.setUserDto(userDto);

        return disposedRepository.save(disposedDto);
    }

    /**
     * Find multiple dispose by name
     *
     * @return A list of disposes made by the given user
     */
    public List<DisposedDto> getDisposes(UserDto userDto) {
        return disposedRepository.findAllByUserDto(userDto);
    }


    /**
     * Find all disposes between fromDate and toDate
     *
     * @param fromDateConverted start of time window of the dispose list
     * @param toDateConverted   end of time window of the dispose list
     * @return A list of disposes in the given time
     */
    public List<DisposedDto> findAllByCreatedOnBetween(OffsetDateTime fromDateConverted, OffsetDateTime toDateConverted) {
        return disposedRepository.findAllByCreatedOnBetween(fromDateConverted, toDateConverted);
    }
}
