Feature: Simple Stock management

  Background:
    * url baseUrl
    # data used for testing later on
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

  Scenario: Create a stock item
    # Create stock item
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

    # Get the item that was just created
    Given path '/stock/' + stockId
    When method GET
    Then status 200
    And match response contains { id: #(stockId), name: #(name), unitType: #(unitType), quantity: #(quantity), pricePerUnit: #(pricePerUnit) }
    And match response.isDeleted == false

  Scenario: Update a stock item with all values
    # Create Item
    Given path '/stock'
    And request { name: #(name), unitType: #(unitType), quantity: #(quantity), pricePerUnit: #(pricePerUnit) }
    When method POST
    Then status 201
    And def stockId = response.id

    # Update this stock item
    Given path 'stock', stockId
    And request { name: #(nameChanged), unitType: #(unitTypeChanged), quantity: #(quantityChanged), pricePerUnit: #(pricePerUnitChanged) }
    When method PATCH
    Then status 200
    And match response contains { id: #(stockId), name: #(nameChanged), unitType: #(unitTypeChanged), quantity: #(quantityChanged), pricePerUnit: #(pricePerUnitChanged) }

    # Check that patch was successful
    Given path '/stock/' + stockId
    When method GET
    Then status 200
    And match response contains { id: #(stockId), name: #(nameChanged), unitType: #(unitTypeChanged), quantity: #(quantityChanged), pricePerUnit: #(pricePerUnitChanged) }
    And match response.isDeleted == false

  Scenario: Patch of only the name works
    # Create Item
    Given path '/stock'
    And request { name: #(name), unitType: #(unitType), quantity: #(quantity), pricePerUnit: #(pricePerUnit) }
    When method POST
    Then status 201
    And def stockId = response.id

    # Only patch name of this item
    Given path '/stock/' + stockId
    And def nameChanged = "Juniper"
    And request { name: #(nameChanged) }
    When method PATCH
    Then status 200
    And match response contains { id: #(stockId), name: #(nameChanged), unitType: #(unitType), quantity: #(quantity), pricePerUnit: #(pricePerUnit) }

  Scenario: Patch of only the UnitType works
    # Create Item
    Given path '/stock'
    And request { name: #(name), unitType: #(unitType), quantity: #(quantity), pricePerUnit: #(pricePerUnit) }
    When method POST
    Then status 201
    And def stockId = response.id

    # Only patch UnitType
    Given path '/stock/' + stockId
    And def unitTypeChanged = "WEIGHT"
    And request { unitType: #(unitTypeChanged) }
    When method PATCH
    Then status 200
    And match response contains { id: #(stockId), name: #(name), unitType: #(unitTypeChanged), quantity: #(quantity), pricePerUnit: #(pricePerUnit) }

  Scenario: Patch of only the quantity works
    # Create Item
    Given path '/stock'
    And request { name: #(name), unitType: #(unitType), quantity: #(quantity), pricePerUnit: #(pricePerUnit) }
    When method POST
    Then status 201
    And def stockId = response.id

    # Only patch quantity
    Given path '/stock/' + stockId
    And def quantityChanged = 120.0
    And request { quantity: #(quantityChanged) }
    When method PATCH
    Then status 200
    And match response contains { id: #(stockId), name: #(name), unitType: #(unitType), quantity: #(quantityChanged), pricePerUnit: #(pricePerUnit) }

  Scenario: Patch of only the pricePerUnit works
    # Create Item
    Given path '/stock'
    And request { name: #(name), unitType: #(unitType), quantity: #(quantity), pricePerUnit: #(pricePerUnit) }
    When method POST
    Then status 201
    And def stockId = response.id

    # Only patch pricePerUnit
    Given path '/stock/' + stockId
    And def pricePerUnitChanged = 1.22
    And request { pricePerUnit: #(pricePerUnitChanged) }
    When method PATCH
    Then status 200
    And match response contains { id: #(stockId), name: #(name), unitType: #(unitType), quantity: #(quantity), pricePerUnit: #(pricePerUnitChanged) }

  Scenario: Soft Delete works
    # Create Item
    Given path '/stock'
    And request { name: #(name), unitType: #(unitType), quantity: #(quantity), pricePerUnit: #(pricePerUnit) }
    When method POST
    Then status 201
    And def stockId = response.id

    # Delete this item
    Given path '/stock/' + stockId
    When method DELETE
    Then status 204

    # Get item after delete
    Given path '/stock/' + stockId
    When method GET
    Then status 200
    And match response contains { id: #(stockId), name: #(name), unitType: #(unitType), quantity: #(quantity), pricePerUnit: #(pricePerUnit) }
    And match response.isDeleted == true

  Scenario: POST can create an item without a description
    Given path '/stock'
    And request { name: "test", unitType: "PIECE", quantity: 10.0, pricePerUnit: 5.0 }
    When method POST
    Then status 201

  Scenario: Cannot create item with unitType PIECE and fractional quantity
    Given path '/stock'
    And request { name: #(name), unitType: "PIECE", quantity: 14.5, pricePerUnit: 4.2 }
    When method POST
    Then status 400
    And response.errorCode == 400008

  Scenario: POST with same item name is possible
    # First Post
    Given path '/stock'
    And request { name: #(name), unitType: #(unitType), quantity: #(quantity), pricePerUnit: #(pricePerUnit) }
    When method POST
    Then status 201
    And assert response.id != null
    And assert response.name == name
    And assert response.unitType == unitType
    And assert response.quantity == quantity
    And assert response.pricePerUnit == pricePerUnit
    And def stockId = response.id

    # Second Post
    Given path '/stock'
    And request { name: #(name), unitType: #(unitType), quantity: #(quantity), pricePerUnit: #(pricePerUnit) }
    When method POST
    Then status 201
    And assert response.id != stockId
    And assert response.name == name
    And assert response.unitType == unitType
    And assert response.quantity == quantity
    And assert response.pricePerUnit == pricePerUnit

  Scenario: Cannot PATCH an item with unitType PIECE and fractional quantity
    # Create Item
    Given path '/stock'
    And request { name: #(name), unitType: #(unitType), quantity: #(quantity), pricePerUnit: #(pricePerUnit) }
    When method POST
    Then status 201
    And assert response.id != null
    And assert response.name == name
    And assert response.unitType == unitType
    And assert response.quantity == quantity
    And assert response.pricePerUnit == pricePerUnit
    And def stockId = response.id

    # Patch it
    Given path '/stock/' + stockId
    And request { unitType: "PIECE", quantity: 7.4 }
    When method PATCH
    Then status 400
    And response.errorCode == 400008

    # Check that no values were updated
    Given path '/stock/' + stockId
    When method GET
    Then status 200
    And match response contains { id: #(stockId), name: #(name), unitType: #(unitType), quantity: #(quantity), pricePerUnit: #(pricePerUnit) }
    And match response.isDeleted == false

  Scenario: Cannot PATCH fractional quantity of item with unitType PIECE
    # Create Item
    Given path '/stock'
    And request { name: #(name), unitType: "PIECE", quantity: #(quantity), pricePerUnit: #(pricePerUnit) }
    When method POST
    Then status 201
    And assert response.id != null
    And assert response.name == name
    And assert response.unitType == unitType
    And assert response.quantity == quantity
    And assert response.pricePerUnit == pricePerUnit
    And def stockId = response.id

    # Patch Item
    Given path '/stock/' + stockId
    And request { quantity: 7.4 }
    When method PATCH
    Then status 400
    And response.errorCode == 400008

    # Check that no values were updated
    Given path '/stock/' + stockId
    When method GET
    Then status 200
    And match response contains { id: #(stockId), name: #(name), unitType: "PIECE", quantity: #(quantity), pricePerUnit: #(pricePerUnit) }
    And match response.isDeleted == false

  Scenario: Cannot PATCH a soft deleted item
    # Create Item
    Given path '/stock'
    And request { name: #(name), unitType: #(unitType), quantity: #(quantity), pricePerUnit: #(pricePerUnit) }
    When method POST
    Then status 201
    And assert response.id != null
    And assert response.name == name
    And assert response.unitType == unitType
    And assert response.quantity == quantity
    And assert response.pricePerUnit == pricePerUnit
    And def stockId = response.id

    # Delete it
    Given path '/stock/' + stockId
    When method DELETE
    Then status 204

    # Check that it can't be patched
    Given path '/stock/' + stockId
    And request { name: "Honey" }
    When method PATCH
    Then status 400
    And assert response.errorCode == 400009

  Scenario: GET with no delete parameter does not include deleted items
    # Create item 1
    Given path '/stock'
    And request { name: "Bananas", unitType: "WEIGHT", quantity: 140.0, pricePerUnit: 1.3 }
    When method POST
    Then status 201
    And def stockId1 = response.id

    # Create item 2
    Given path '/stock'
    And request { name: "Pumpkin", unitType: "PIECE", quantity: 20.0, pricePerUnit: 4.3 }
    When method POST
    Then status 201
    And def stockId2 = response.id

    # Delete item 2
    Given path '/stock/' + stockId2
    When method DELETE
    Then status 204

    # GET items
    Given path '/stock'
    When method GET
    Then status 200
    And match each response.items contains { isDeleted: false }

  Scenario: GET with delete parameter OMIT does not include deleted items
    # Create item 1
    Given path '/stock'
    And request { name: "Bananas", unitType: "WEIGHT", quantity: 140.0, pricePerUnit: 1.3 }
    When method POST
    Then status 201
    And def stockId1 = response.id

    # Create item 2
    Given path '/stock'
    And request { name: "Pumpkin", unitType: "PIECE", quantity: 20.0, pricePerUnit: 4.3 }
    When method POST
    Then status 201
    And def stockId2 = response.id

    # Delete item 2
    Given path '/stock/' + stockId2
    When method DELETE
    Then status 204

    # GET items
    Given path '/stock'
    And param deleted = "OMIT"
    When method GET
    Then status 200
    And match each response.items contains { isDeleted: false }

  Scenario: GET with delete parameter INCLUDE includes deleted and not deleted items
    # Create item 1
    Given path '/stock'
    And request { name: "Bananas", unitType: "WEIGHT", quantity: 140.0, pricePerUnit: 1.3 }
    When method POST
    Then status 201
    And def stockId1 = response.id

    # Create item 2
    Given path '/stock'
    And request { name: "Pumpkin", unitType: "PIECE", quantity: 20.0, pricePerUnit: 4.3 }
    When method POST
    Then status 201
    And def stockId2 = response.id

    # Delete item 2
    Given path '/stock/' + stockId2
    When method DELETE
    Then status 204

    # GET items
    Given path '/stock'
    And param deleted = "INCLUDE"
    When method GET
    Then status 200
    And def filterForDeleted = function(x){ return x.isDeleted == true }
    And def filterForNotDeleted = function(x){ return x.isDeleted == false }
    And def deletedItems = karate.filter(response.items, filterForDeleted)
    And def notDeletedItems = karate.filter(response.items, filterForNotDeleted)
    And assert deletedItems.length > 0
    And assert notDeletedItems.length > 0

  Scenario: GET with delete parameter ONLY does not include non-deleted items
    # Create item 1
    Given path '/stock'
    And request { name: "Bananas", unitType: "WEIGHT", quantity: 140.0, pricePerUnit: 1.3 }
    When method POST
    Then status 201
    And def stockId1 = response.id

    # Create item 2
    Given path '/stock'
    And request { name: "Pumpkin", unitType: "PIECE", quantity: 20.0, pricePerUnit: 4.3 }
    When method POST
    Then status 201
    And def stockId2 = response.id

    # Delete item 2
    Given path '/stock/' + stockId2
    When method DELETE
    Then status 204

    # GET items
    Given path '/stock'
    And param deleted = "ONLY"
    When method GET
    Then status 200
    And match each response.items contains { isDeleted: true }
