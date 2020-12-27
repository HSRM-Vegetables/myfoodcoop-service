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
    * def pricePerUnit = 4.2
    * def description = "this is a lovely piece of produce"
    * def nameChanged = 'Avocados'
    * def unitTypeChanged = 'WEIGHT'
    * def quantityChanged = 110.0
    * def pricePerUnitChanged = 4.2

  Scenario: GET returns an empty list if no stock exists
    Given path '/stock'
    When method GET
    Then status 200
    And assert response.items.length == 0

  Scenario: POST creates an item in the stock and returns it
    Given path '/stock'
    And request { name: #(name), unitType: #(unitType), quantity: #(quantity), pricePerUnit: #(pricePerUnit), description: #(description) }
    When method POST
    Then status 201
    And assert response.id != null
    And assert response.name == name
    And assert response.unitType == unitType
    And assert response.quantity == quantity
    And assert response.pricePerUnit == pricePerUnit
    And assert response.description == description
    And def stockId = response.id

  Scenario: POST can create an item without a description
    Given path '/stock'
    And request { name: "test", unitType: "PIECE", quantity: 10.0, pricePerUnit: 5.0 }
    When method POST
    Then status 201

  Scenario: GET returns items when they exist in stock
    Given path '/stock'
    When method GET
    Then status 200
    And assert response.items.length > 0
    # TODO: might not be semantically correct
    And match any response.items contains { id: #(stockId), name: #(name), unitType: #(unitType), quantity: #(quantity), pricePerUnit: #(pricePerUnit) }

  Scenario: GET a specific stock item
    Given path '/stock/' + stockId
    When method GET
    Then status 200
    And match response contains { id: #(stockId), name: #(name), unitType: #(unitType), quantity: #(quantity), pricePerUnit: #(pricePerUnit) }
    And match response.isDeleted == false

  Scenario: PATCH a stock item
    Given path 'stock', stockId
    And request { name: #(nameChanged), unitType: #(unitTypeChanged), quantity: #(quantityChanged), pricePerUnit: #(pricePerUnitChanged) }
    When method PATCH
    Then status 200
    And match response contains { id: #(stockId), name: #(nameChanged), unitType: #(unitTypeChanged), quantity: #(quantityChanged), pricePerUnit: #(pricePerUnitChanged) }

  Scenario: GET a specific stock item after PATCH
    Given path '/stock/' + stockId
    When method GET
    Then status 200
    And match response contains { id: #(stockId), name: #(nameChanged), unitType: #(unitTypeChanged), quantity: #(quantityChanged), pricePerUnit: #(pricePerUnitChanged) }
    And match response.isDeleted == false

  Scenario: Only patch name
    Given path '/stock/' + stockId
    And def nameChanged = "Juniper"
    And request { name: #(nameChanged) }
    When method PATCH
    Then status 200
    And match response contains { id: #(stockId), name: #(nameChanged), unitType: #(unitTypeChanged), quantity: #(quantityChanged), pricePerUnit: #(pricePerUnitChanged) }

  Scenario: Only patch unitType
    Given path '/stock/' + stockId
    And def unitTypeChanged = "WEIGHT"
    And request { unitType: #(unitTypeChanged) }
    When method PATCH
    Then status 200
    And match response contains { id: #(stockId), name: #(nameChanged), unitType: #(unitTypeChanged), quantity: #(quantityChanged), pricePerUnit: #(pricePerUnitChanged) }

  Scenario: Only patch quantity
    Given path '/stock/' + stockId
    And def quantityChanged = 120.0
    And request { quantity: #(quantityChanged) }
    When method PATCH
    Then status 200
    And match response contains { id: #(stockId), name: #(nameChanged), unitType: #(unitTypeChanged), quantity: #(quantityChanged), pricePerUnit: #(pricePerUnitChanged) }

  Scenario: Only patch pricePerUnit
    Given path '/stock/' + stockId
    And def pricePerUnitChanged = 1.22
    And request { pricePerUnit: #(pricePerUnitChanged) }
    When method PATCH
    Then status 200
    And match response contains { id: #(stockId), name: #(nameChanged), unitType: #(unitTypeChanged), quantity: #(quantityChanged), pricePerUnit: #(pricePerUnitChanged) }

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
    And match response contains { id: #(stockId), name: #(nameChanged), unitType: #(unitTypeChanged), quantity: #(quantityChanged), pricePerUnit: #(pricePerUnitChanged) }
    And match response.isDeleted == true
