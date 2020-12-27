Feature: Simple Purchases

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

  Scenario: Create a balance for a user
    Given path '/balance/Robby'
    And request { balance: 500 }
    When method PATCH
    Then status 200

  Scenario: Purchase a single item
    Given path '/purchase/Robby'
    And def item1 = { id: #(stockId1), amount: 1 }
    And request { items: [#(item1)] }
    When method POST
    Then status 200
    And assert response.name == "Robby"
    And assert response.price == 1.3

  Scenario: Purchase reduces stock
    Given path '/stock/' + stockId1
    When method GET
    Then status 200
    And match response contains { quantity: 139.0 }

  Scenario: Purchase multiple items
    Given path '/purchase/Robby'
    And def item1 = { id: #(stockId1), amount: 1 }
    And def item3 = { id: #(stockId3), amount: 1 }
    And request { items: [#(item1), #(item3)] }
    When method POST
    Then status 200
    And assert response.name == "Robby"
    And assert response.price == 5.6

  Scenario: Purchase reduces stock on stockItem1
    Given path '/stock/' + stockId1
    When method GET
    Then status 200
    And match response contains { quantity: 138.0 }

  Scenario: Purchase reduces stock on stockItem3
    Given path '/stock/' + stockId3
    When method GET
    Then status 200
    And match response contains { quantity: 19.0 }

  Scenario: Balance is reduced for user after purchase
    Given path '/balance/Robby'
    When method GET
    Then status 200
    Then assert response.balance < 500

