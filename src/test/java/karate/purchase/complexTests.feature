Feature: Complex Purchases

  #
  # The order of these tests is important!
  #

  Background:
    * url baseUrl
    # data used for testing later on
    * def stockId1 = "1234"
    * def stockId2 = "5678"
    * def stockId3 = "91011"
    * def stockId4 = "1213"

  Scenario: Create first stock item
    Given path '/stock'
    And request { name: "Bananas", unitType: "WEIGHT", quantity: 140.0, pricePerUnit: 1.3 }
    When method POST
    Then status 201
    And def stockId1 = response.id

  Scenario: Create second stock item
    Given path '/stock'
    And request { name: "Potatoes", unitType: "WEIGHT", quantity: 100.0, pricePerUnit: 2.2 }
    When method POST
    Then status 201
    And def stockId2 = response.id

  Scenario: Create third stock item
    Given path '/stock'
    And request { name: "Pumpkin", unitType: "PIECE", quantity: 20.0, pricePerUnit: 4.3 }
    When method POST
    Then status 201
    And def stockId1 = response.id

  Scenario: Create fourth stock item
    Given path '/stock'
    And request { name: "Honey", unitType: "PIECE", quantity: 10.0, pricePerUnit: 5.0 }
    When method POST
    Then status 201
    And def stockId1 = response.id

  Scenario: DELETE fourth stock item
    Given path '/stock/' + stockId4
    When method DELETE
    Then status 204

  Scenario: Create a balance for a user
    Given path '/balance/Robby'
    And request { balance: 500 }
    When method PATCH
    Then status 200

  Scenario: Cannot purchase fractional items when unitType is PIECE
    Given path '/purchase/Robby'
    And def item1 = { id: #(stockId1), amount: 1 }
    And def item3 = { id: #(stockId3), amount: 1.5 }
    And request { items: [#(item1), #(item3)] }
    When method POST
    Then status 400
    # TODO: check for specific errorCode when implemented

  Scenario: Cannot purchase deleted item
    Given path '/purchase/Robby'
    And def item1 = { id: #(stockId1), amount: 1 }
    And def item4 = { id: #(stockId4), amount: 1 }
    And request { items: [#(item1), #(item3)] }
    When method POST
    Then status 400
    # TODO: check for specific errorCode when implemented

  Scenario: Purchase a single item with higher amount than in stock is possible
    Given path '/purchase/Robby'
    And def item1 = { id: #(stockId1), amount: 200 }
    And request { items: [#(item1)] }
    When method POST
    Then status 200
    And assert response.name == "Robby"

  Scenario: Unknown user cannot make a purchase
    Given path '/purchase/Unknown'
    And def item1 = { id: #(stockId1), amount: 1 }
    And request { items: [#(item1)] }
    When method POST
    Then status 400
    # TODO: check for specific errorCode when implemented

  Scenario: Cannot have multiple items with same id in items array
    Given path '/purchase/Robby'
    And def item1 = { id: #(stockId1), amount: 1 }
    And def item1AsWell = { id: #(stockId1), amount: 5 }
    And request { items: [#(item1), #(item1AsWell)] }
    When method POST
    Then status 400
    # TODO: check for specific errorCode when implemented
