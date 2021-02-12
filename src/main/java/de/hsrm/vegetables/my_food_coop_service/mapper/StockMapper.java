package de.hsrm.vegetables.my_food_coop_service.mapper;

import de.hsrm.vegetables.my_food_coop_service.domain.dto.DisposedDto;
import de.hsrm.vegetables.my_food_coop_service.domain.dto.StockDto;
import de.hsrm.vegetables.my_food_coop_service.model.DisposedItem;
import de.hsrm.vegetables.my_food_coop_service.model.StockResponse;

import java.util.List;
import java.util.stream.Collectors;

public class StockMapper {

    // Hide implicit public constructor
    private StockMapper() {
    }

    public static StockResponse stockDtoToStockResponse(StockDto stockDto) {
        StockResponse response = new StockResponse();
        response.id(stockDto.getId());
        response.name(stockDto.getName());
        response.unitType(stockDto.getUnitType());
        response.quantity(stockDto.getQuantity());
        response.pricePerUnit(stockDto.getPricePerUnit());
        response.description(stockDto.getDescription());
        response.setIsDeleted(stockDto.isDeleted());
        response.sustainablyProduced(stockDto.isSustainablyProduced());
        response.certificates(stockDto.getCertificates());
        response.originCategory(stockDto.getOriginCategory());
        response.producer(stockDto.getProducer());
        response.supplier(stockDto.getSupplier());
        response.orderDate(stockDto.getOrderDate());
        response.deliveryDate(stockDto.getDeliveryDate());
        response.setStockStatus(stockDto.getStockStatus());
        response.setVat(stockDto.getVat());
        return response;
    }

    public static List<StockResponse> listStockDtoToListStockResponse(List<StockDto> stockDtos) {
        return stockDtos
                .stream()
                .map(StockMapper::stockDtoToStockResponse)
                .collect(Collectors.toList());
    }

    public static DisposedItem disposedDtoToDisposedItem(DisposedDto disposedDto) {
        DisposedItem disposedItem = new DisposedItem();

        disposedItem.setAmount(disposedDto.getAmount());
        disposedItem.setName(disposedDto.getStockDto()
                .getName());
        disposedItem.setVat(disposedDto.getVat());
        disposedItem.setUnitType(disposedDto.getUnitType());
        disposedItem.setPricePerUnit(disposedDto.getPricePerUnit());
        disposedItem.setCreatedOn(disposedDto.getCreatedOn());
        disposedItem.setStockId(disposedDto.getStockDto()
                .getId());
        disposedItem.setUserId(disposedDto.getUserDto()
                .getId());

        return disposedItem;
    }
}
