Feature: Simple Stock management

  #
  # The order of these tests is important!
  #

  Background:
    * url baseUrl
    # data used for testing later on
    * def stockId = "1234"
    * def name = 'Bananas'
    * def unitType = 'PIECE'
    * def quantity = 42.0
    * def pricePerItem = 4.2
    * def nameChanged = 'Avocados'
    * def unitTypeChanged = 'WEIGHT'
    * def quantityChanged = 110.0
    * def pricePerItemChanged = 4.2

  Scenario: GET returns an empty list if no stock exists
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

  Scenario: GET returns items when they exist in stock
    Given path '/stock'
    When method GET
    Then status 200
    And assert response.items.length > 0
    # TODO: might not be semantically correct
    And match any response.items contains { id: #(stockId), name: #(name), unitType: #(unitType), quantity: #(quantity), pricePerItem: #(pricePerItem) }

  Scenario: GET a specific stock item
    Given path '/stock/' + stockId
    When method GET
    Then status 200
    And match response contains { id: #(stockId), name: #(name), unitType: #(unitType), quantity: #(quantity), pricePerItem: #(pricePerItem) }
    And match response.isDeleted == false

  Scenario: PATCH a stock item
    Given path 'stock', stockId
    And request { name: #(nameChanged), unitType: #(unitTypeChanged), quantity: #(quantityChanged), pricePerItem: #(pricePerItemChanged) }
    When method PATCH
    Then status 200
    And match response contains { id: #(stockId), name: #(nameChanged), unitType: #(unitTypeChanged), quantity: #(quantityChanged), pricePerItem: #(pricePerItemChanged) }

  Scenario: GET a specific stock item after PATCH
    Given path '/stock/' + stockId
    When method GET
    Then status 200
    And match response contains { id: #(stockId), name: #(nameChanged), unitType: #(unitTypeChanged), quantity: #(quantityChanged), pricePerItem: #(pricePerItemChanged) }
    And match response.isDeleted == false

  Scenario: Only patch name
    Given path '/stock/' + stockId
    And def nameChanged = "Juniper"
    And request { name: #(nameChanged) }
    When method PATCH
    Then status 200
    And match response contains { id: #(stockId), name: #(nameChanged), unitType: #(unitTypeChanged), quantity: #(quantityChanged), pricePerItem: #(pricePerItemChanged) }

  Scenario: Only patch unitType
    Given path '/stock/' + stockId
    And def unitTypeChanged = "WEIGHT"
    And request { unitType: #(unitTypeChanged) }
    When method PATCH
    Then status 200
    And match response contains { id: #(stockId), name: #(nameChanged), unitType: #(unitTypeChanged), quantity: #(quantityChanged), pricePerItem: #(pricePerItemChanged) }

  Scenario: Only patch quantity
    Given path '/stock/' + stockId
    And def quantityChanged = 120.0
    And request { quantity: #(quantityChanged) }
    When method PATCH
    Then status 200
    And match response contains { id: #(stockId), name: #(nameChanged), unitType: #(unitTypeChanged), quantity: #(quantityChanged), pricePerItem: #(pricePerItemChanged) }

  Scenario: Only patch pricePerItem
    Given path '/stock/' + stockId
    And def pricePerItemChanged = 1.22
    And request { pricePerItem: #(pricePerItemChanged) }
    When method PATCH
    Then status 200
    And match response contains { id: #(stockId), name: #(nameChanged), unitType: #(unitTypeChanged), quantity: #(quantityChanged), pricePerItem: #(pricePerItemChanged) }

  Scenario: DELETE a stock item
    Given path '/stock/' + stockId
    When method DELETE
    Then status 204

  Scenario: GET returns no items after deleting all stock items
    Given path '/stock'
    When method GET
    Then status 200
    And assert response.items.length == 0

  Scenario: GET a specific stock item after DELETE
    Given path '/stock/' + stockId
    When method GET
    Then status 200
    And match response contains { id: #(stockId), name: #(nameChanged), unitType: #(unitTypeChanged), quantity: #(quantityChanged), pricePerItem: #(pricePerItemChanged) }
    And match response.isDeleted == true
