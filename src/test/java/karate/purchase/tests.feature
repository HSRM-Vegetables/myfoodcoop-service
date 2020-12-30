Feature: Simple Purchases

  Background:
    * url baseUrl
    * def findItemWithId =
    """
    function(arr, id) {
      return arr.find(function(item) { item.id === id });
    }
    """

    * def calcPrice =
    """
    function (items) {
      return items.reduce(function(acc, curr) { return acc + ( curr.amount * curr.pricePerUnit ); }, 0)
    }
    """

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
    Given path '/purchase'
    And header X-Username = "Robby"
    And def item1 = { id: #(stockId1), amount: 1 }
    And request { items: [#(item1)] }
    When method POST
    Then status 200
    And match response contains { id: '#uuid', name: '#string', balance: '#number', price: '#number' }
    And assert response.name == "Robby"
    And assert response.price == 1.3
    And def purchaseId = response.id

    # Check that stock was reduced
    Given path '/stock/' + stockId1
    When method GET
    Then status 200
    And match response contains { quantity: 139.0 }

    # Check that the balance was reduced
    Given path '/balance/Robby'
    When method GET
    Then status 200
    Then assert response.balance == 498.7

    # Check that purchase exists
    Given path '/purchase', purchaseId
    When method GET
    Then status 200
    And match response contains { id: '#uuid', createdOn: '#string', totalPrice: '#number', items: '#array'}
    And match each response.items contains { id: '#uuid', name: '#string', amount: '#number', pricePerUnit: '#number', unitType: '#string' }
    And def calculatedPrice = calcPrice(response.items)
    And assert response.totalPrice == calculatedPrice

    # Check that purchase exists in purchase list
    Given path '/purchase'
    And header X-Username = "Robby"
    When method GET
    Then status 200
    And assert response.purchases.length == 1
    And def purchase = findItemWithId(response.purchases, purchaseId)
    And match purchase contains { id: #(purchaseId) }
    And assert purchase.items.length == 1
    And def purchasedItem1 = findItemWithId(purchase.items, stockId1)
    And match purchasedItem1 contains { id: #(stockId1) }

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
    Given path '/purchase'
    And header X-Username = "Robby"
    And def item1 = { id: #(stockId1), amount: 1 }
    And def item2 = { id: #(stockId2), amount: 1 }
    And request { items: [#(item1), #(item2)] }
    When method POST
    Then status 200
    And match response contains { id: '#uuid', name: '#string', balance: '#number', price: '#number' }
    And assert response.name == "Robby"
    And assert response.price == 5.6
    And def purchaseId = response.id

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
    Then assert response.balance == 494.4

    # Check that purchase exists
    Given path '/purchase', purchaseId
    When method GET
    Then status 200
    And match response contains { id: '#uuid', createdOn: '#string', totalPrice: '#number', items: '#array'}
    And match each response.items contains { id: '#uuid', name: '#string', amount: '#number', pricePerUnit: '#number', unitType: '#string' }
    And def calculatedPrice = calcPrice(response.items)
    And assert response.totalPrice == calculatedPrice

    # Check that purchase exists in list
    Given path '/purchase'
    And header X-Username = "Robby"
    When method GET
    Then status 200
    And assert response.purchases.length == 1
    And def purchase = findItemWithId(response.purchases, purchaseId)
    And match purchase contains { id: #(purchaseId) }
    And assert purchase.items.length == 2
    And def purchasedItem1 = findItemWithId(purchase.items, stockId1)
    And match purchasedItem1 contains { id: #(stockId1) }
    And def purchasedItem2 = findItemWithId(purchase.items, stockId2)
    And match purchasedItem2 contains { id: #(stockId2) }

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
    Given path '/purchase'
    And header X-Username = "Robby"
    And def item1 = { id: #(stockId1), amount: 1 }
    And def item2 = { id: #(stockId2), amount: 1.5 }
    And request { items: [#(item1), #(item2)] }
    When method POST
    Then status 400
    And assert response.errorCode == 400008

    # Check quantity didn't change
    Given path '/stock/' + stockId1
    When method GET
    Then status 200
    And match response contains { quantity: 140.0 }

    # Check quantity didn't change
    Given path '/stock/' + stockId2
    When method GET
    Then status 200
    And match response contains { quantity: 20.0 }

    # Check that the balance wasn't reduced
    Given path '/balance/Robby'
    When method GET
    Then status 200
    Then assert response.balance == 500

    # Check that purchase doesnt exist
    Given path '/purchase'
    And header X-Username = "Robby"
    When method GET
    Then status 200
    And assert response.purchases.length == 0

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

    # Purchase items
    Given path '/purchase'
    And header X-Username = "Robby"
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

    # Check that purchase doesnt exist
    Given path '/purchase'
    And header X-Username = "Robby"
    When method GET
    Then status 200
    And assert response.purchases.length == 0

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
    Given path '/purchase'
    And header X-Username = "Robby"
    And def item1 = { id: #(stockId1), amount: 200 }
    And request { items: [#(item1)] }
    When method POST
    Then status 200
    And match response contains { id: '#uuid', name: '#string', balance: '#number', price: '#number' }
    And assert response.name == "Robby"
    And def purchaseId = response.id

    # Check quantity is below 0
    Given path '/stock/' + stockId1
    When method GET
    Then status 200
    And assert response.quantity == -60

    # Check that the balance was reduced
    Given path '/balance/Robby'
    When method GET
    Then status 200
    Then assert response.balance < 500

    # Check that purchase exists
    Given path '/purchase', purchaseId
    When method GET
    Then status 200
    And match response contains { id: '#uuid', createdOn: '#string', totalPrice: '#number', items: '#array'}
    And match each response.items contains { id: '#uuid', name: '#string', amount: '#number', pricePerUnit: '#number', unitType: '#string' }
    And def calculatedPrice = calcPrice(response.items)
    And assert response.totalPrice == calculatedPrice

    # Check that purchase exists in list
    Given path '/purchase'
    And header X-Username = "Robby"
    When method GET
    Then status 200
    And assert response.purchases.length == 1
    And def purchase = findItemWithId(response.purchases, purchaseId)
    And match purchase contains { id: #(purchaseId) }
    And assert purchase.items.length == 1
    And def purchasedItem1 = findItemWithId(purchase.items, stockId1)
    And match purchasedItem1 contains { id: #(stockId1) }

  Scenario: Unknown user cannot make a purchase
    # Create item 1
    Given path '/stock'
    And request { name: "Bananas", unitType: "WEIGHT", quantity: 140.0, pricePerUnit: 1.3 }
    When method POST
    Then status 201
    And def stockId1 = response.id

    # Purchase
    Given path '/purchase'
    And header X-Username = "Unknown"
    And def item1 = { id: #(stockId1), amount: 1 }
    And request { items: [#(item1)] }
    When method POST
    Then status 404
    And assert response.errorCode == 404002

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
    Given path '/purchase'
    And header X-Username = "Robby"
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

    # Check that purchase doesnt exist
    Given path '/purchase'
    And header X-Username = "Robby"
    When method GET
    Then status 200
    And assert response.purchases.length == 0

  Scenario: The price of a purchase does not change after a stock items price was updated
    # Create Balance for User
    Given path '/balance/Robby'
    And request { balance: 500 }
    When method PATCH
    Then status 200

    # Create item
    Given path '/stock'
    And request { name: "Bananas", unitType: "WEIGHT", quantity: 140.0, pricePerUnit: 1.0 }
    When method POST
    Then status 201
    And def stockId1 = response.id

    # Purchase item
    Given path '/purchase'
    And header X-Username = "Robby"
    And def item1 = { id: #(stockId1), amount: 1 }
    And request { items: [#(item1)] }
    When method POST
    Then status 200
    And match response contains { id: '#uuid', name: '#string', balance: '#number', price: '#number' }
    And def purchaseId = response.id

    # Get the purchase
    Given path '/purchase', purchaseId
    When method GET
    Then status 200
    And match response contains { id: '#uuid', createdOn: '#string', totalPrice: '#number', items: '#array'}
    And match each response.items contains { id: '#uuid', name: '#string', amount: '#number', pricePerUnit: '#number', unitType: '#string' }
    And def totalPriceFirstTime = response.totalPrice
    And def calculatedPriceFirstTime = calcPrice(response.items)

    # Update price of an item
    Given path '/stock/' + stockId1
    And request { pricePerUnit: 5.0 }
    When method PATCH
    Then status 200
    And match response contains { id: #(stockId), name: #(name), unitType: #(unitType), quantity: #(quantity), pricePerUnit: 5 }

    # Get the purchase a second time
    Given path '/purchase', purchaseId
    When method GET
    Then status 200
    And match response contains { id: '#uuid', createdOn: '#string', totalPrice: '#number', items: '#array'}
    And match each response.items contains { id: '#uuid', name: '#string', amount: '#number', pricePerUnit: '#number', unitType: '#string' }
    And def totalPriceSecondTime = response.totalPrice
    And def calculatedPriceSecondTime = calcPrice(response.items)

    # Check prices are still the same
    And assert totalPriceFirstTime == totalPriceSecondTime
    And assert calculatedPriceFirstTime == calculatedPriceSecondTime
