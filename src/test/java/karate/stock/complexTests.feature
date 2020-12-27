Feature: Complex Stock management

  #
  # The order of these tests is important!
  #

  Background:
    * url baseUrl
    # data used for testing later on
    * def stockId = "1234"
    * def name = 'Bananas'
    * def unitType = 'WEIGHT'
    * def quantity = 42.0
    * def pricePerItem = 4.2

  Scenario: Cannot create item with unitType PIECE and fractional quantity
    Given path '/stock'
    And request { name: #(name), unitType: "PIECE", quantity: 14.5, pricePerItem: 4.2 }
    When method POST
    Then status 400
    # TODO: Check for specific error code once implemented

  Scenario: GET returns an empty list
    Given path '/stock'
    When method GET
    Then status 200
    And assert response.items.length == 0

  Scenario: POST creates an item in the stock and returns it
    Given path '/stock'
    And request { name: #(name), unitType: #(unitType), quantity: #(quantity), pricePerItem: #(pricePerItem) }
    When method POST
    Then status 201
    And assert response.id != null
    And assert response.name == name
    And assert response.unitType == unitType
    And assert response.quantity == quantity
    And assert response.pricePerItem == pricePerItem
    And def stockId = response.id

  Scenario: POST with same item name is possible
    Given path '/stock'
    And request { name: #(name), unitType: #(unitType), quantity: #(quantity), pricePerItem: #(pricePerItem) }
    When method POST
    Then status 201
    And assert response.id != stockId
    And assert response.name == name
    And assert response.unitType == unitType
    And assert response.quantity == quantity
    And assert response.pricePerItem == pricePerItem

  Scenario: Cannot PATCH an item with unitType PIECE and fractional quantity
    Given path '/stock/' + stockId
    And request { unitType: "PIECE", quantity: 7.4 }
    When method PATCH
    Then status 400
    # TODO: Check for specific error code once implemented

  Scenario: Previous PATCH did not update values
    Given path '/stock/' + stockId
    When method GET
    Then status 200
    And match response contains { id: #(stockId), name: #(name), unitType: #(unitType), quantity: #(quantity), pricePerItem: #(pricePerItem) }
    And match response.isDeleted == false

  Scenario: Update only unitType of item
    Given path '/stock/' + stockId
    And request { unitType: "PIECE" }
    When method PATCH
    Then status 200
    And assert response.unitType == "PIECE"
    And def unitType = response.unitType

  Scenario: Previous PATCH updated correctly
    Given path '/stock/' + stockId
    When method GET
    Then status 200
    And match response contains { id: #(stockId), name: #(name), unitType: #(unitType), quantity: #(quantity), pricePerItem: #(pricePerItem) }
    And match response.isDeleted == false

  Scenario: Cannot PATCH fractional quantity of item with unitType PIECE
    Given path '/stock/' + stockId
    And request { quantity: 7.4 }
    When method PATCH
    Then status 400
    # TODO: Check for specific error code once implemented

  Scenario: Previous PATCH did not update values
    Given path '/stock/' + stockId
    When method GET
    Then status 200
    And match response contains { id: #(stockId), name: #(name), unitType: #(unitType), quantity: #(quantity), pricePerItem: #(pricePerItem) }
    And match response.isDeleted == false

  Scenario: DELETE a stock item
    Given path '/stock/' + stockId
    When method DELETE
    Then status 204

  Scenario: Cannot PATCH a soft deleted item
    Given path '/stock/' + stockId
    And request { name: "Honey" }
    When method PATCH
    Then status 400
    # TODO: Check for specific error code once implemented
