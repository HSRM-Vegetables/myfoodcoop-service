Feature: Simple Purchases

  Background:
    * url baseUrl

  Scenario: Purchase a single item
    # Create Balance for User
    Given path '/balance/Robby'
    And request { balance: 500 }
    When method PATCH
    Then status 200

    # Create item
    Given path '/stock'
    And request { name: "Bananas", unitType: "WEIGHT", quantity: 140.0, pricePerUnit: 1.3 }
    When method POST
    Then status 201
    And def stockId1 = response.id

    # Purchase item
    Given path '/purchase/Robby'
    And def item1 = { id: #(stockId1), amount: 1 }
    And request { items: [#(item1)] }
    When method POST
    Then status 200
    And assert response.name == "Robby"
    And assert response.price == 1.3

    # Check that stock was reduced
    Given path '/stock/' + stockId1
    When method GET
    Then status 200
    And match response contains { quantity: 139.0 }

    # Check that the balance was reduced
    Given path '/balance/Robby'
    When method GET
    Then status 200
    Then assert response.balance < 500

  Scenario: Purchase multiple items
    # Create Balance for User
    Given path '/balance/Robby'
    And request { balance: 500 }
    When method PATCH
    Then status 200

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

    # Purchase items
    Given path '/purchase/Robby'
    And def item1 = { id: #(stockId1), amount: 1 }
    And def item2 = { id: #(stockId2), amount: 1 }
    And request { items: [#(item1), #(item2)] }
    When method POST
    Then status 200
    And assert response.name == "Robby"
    And assert response.price == 5.6

    # Check stock was reduced on first item
    Given path '/stock/' + stockId1
    When method GET
    Then status 200
    And match response contains { quantity: 139.0 }

    # Check stock was reduced on second item
    Given path '/stock/' + stockId2
    When method GET
    Then status 200
    And match response contains { quantity: 19.0 }

    # Check that the balance was reduced
    Given path '/balance/Robby'
    When method GET
    Then status 200
    Then assert response.balance < 500

  Scenario: Cannot purchase fractional items when unitType is PIECE
    # Create Balance for User
    Given path '/balance/Robby'
    And request { balance: 500 }
    When method PATCH
    Then status 200

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

    # Purchase item
    Given path '/purchase/Robby'
    And def item1 = { id: #(stockId1), amount: 1 }
    And def item2 = { id: #(stockId2), amount: 1.5 }
    And request { items: [#(item1), #(item2)] }
    When method POST
    Then status 400
    And assert response.errorCode == 400008

    # Check that the balance wasn't reduced
    Given path '/balance/Robby'
    When method GET
    Then status 200
    Then assert response.balance == 500

  Scenario: Cannot purchase deleted item
     # Create Balance for User
    Given path '/balance/Robby'
    And request { balance: 500 }
    When method PATCH
    Then status 200

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

    Given path '/purchase/Robby'
    And def item1 = { id: #(stockId1), amount: 1 }
    And def item2 = { id: #(stockId2), amount: 1 }
    And request { items: [#(item1), #(item2)] }
    When method POST
    Then status 400
    And assert response.errorCode == 400011

    # Check that the balance wasn't reduced
    Given path '/balance/Robby'
    When method GET
    Then status 200
    Then assert response.balance == 500

  Scenario: Purchase a single item with higher amount than in stock is possible
    # Create Balance for User
    Given path '/balance/Robby'
    And request { balance: 500 }
    When method PATCH
    Then status 200

    # Create item 1
    Given path '/stock'
    And request { name: "Bananas", unitType: "WEIGHT", quantity: 140.0, pricePerUnit: 1.3 }
    When method POST
    Then status 201
    And def stockId1 = response.id

    # Purchase Items
    Given path '/purchase/Robby'
    And def item1 = { id: #(stockId1), amount: 200 }
    And request { items: [#(item1)] }
    When method POST
    Then status 200
    And assert response.name == "Robby"

    # Check that the balance was reduced
    Given path '/balance/Robby'
    When method GET
    Then status 200
    Then assert response.balance < 500

  Scenario: Unknown user cannot make a purchase
    # Create item 1
    Given path '/stock'
    And request { name: "Bananas", unitType: "WEIGHT", quantity: 140.0, pricePerUnit: 1.3 }
    When method POST
    Then status 201
    And def stockId1 = response.id

    # Purchase
    Given path '/purchase/Unknown'
    And def item1 = { id: #(stockId1), amount: 1 }
    And request { items: [#(item1)] }
    When method POST
    Then status 404
    # TODO: check for specific errorCode when implemented

  Scenario: Cannot have multiple items with same id in items array
    # Create Balance for User
    Given path '/balance/Robby'
    And request { balance: 500 }
    When method PATCH
    Then status 200

    # Create item 1
    Given path '/stock'
    And request { name: "Bananas", unitType: "WEIGHT", quantity: 140.0, pricePerUnit: 1.3 }
    When method POST
    Then status 201
    And def stockId1 = response.id

    # Purchase  items
    Given path '/purchase/Robby'
    And def item1 = { id: #(stockId1), amount: 1 }
    And def item1AsWell = { id: #(stockId1), amount: 5 }
    And request { items: [#(item1), #(item1AsWell)] }
    When method POST
    Then status 400
    And assert response.errorCode == 400010

    # Check that the balance wasn't reduced
    Given path '/balance/Robby'
    When method GET
    Then status 200
    Then assert response.balance == 500

