package de.hsrm.vegetables.service.mapper;

import de.hsrm.vegetables.Stadtgemuese_Backend.model.StockResponse;
import de.hsrm.vegetables.service.domain.dto.StockDto;

import java.time.LocalDate;
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
        return response;
    }

    public static List<StockResponse> listStockDtoToListStockResponse(List<StockDto> stockDtos) {
        return stockDtos
                .stream()
                .map(StockMapper::stockDtoToStockResponse)
                .collect(Collectors.toList());
    }
}
