Feature: Simple Purchases

  Background:
    * url baseUrl + "/v2"
    * def password = "a_funny_horse**jumps_high778"
    * def findItemWithId =
    """
    function(arr, id) {
      if (arr === undefined || arr === null || id === undefined || id === null) {
        return null;
      }
      for (var i = 0; i < arr.length; i++) {
        if (arr[i].id === id) {
          return arr[i];
        }
      }
      return null;
    }
    """

    * def calcPrice =
    """
    function (arr) {
      if (arr === undefined || arr === null) return 0.0;
      var res = 0.0
      for (var i = 0; i < arr.length; i++) {
        res += arr[i].amount * arr[i].pricePerUnit;
      }
      return res;
    }
    """

  Scenario: Purchase a single item
    # Create Item with Orderer
    Given path 'auth', 'login'
    And request { username: 'orderer',  password: #(password) }
    When method POST
    Then status 200
    And def oToken = response.token

    # Create item
    Given path '/stock'
    And header Authorization = "Bearer " + oToken
    And request { name: "Bananas", unitType: "WEIGHT", quantity: 140.0, pricePerUnit: 1.3, stockStatus: 'INSTOCK' }
    When method POST
    Then status 201
    And def stockId1 = response.id

    # Login with member
    Given path 'auth', 'login'
    And request { username: 'member',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    # Purchase item
    Given path '/purchase'
    And header Authorization = "Bearer " + token
    And def item1 = { id: #(stockId1), amount: 1 }
    And request { items: [#(item1)] }
    When method POST
    Then status 200
    And match response contains { id: '#uuid', name: '#string', balance: '#number', price: '#number' }
    And assert response.name == "member"
    And assert response.price == 1.3
    And def purchaseId = response.id

    # Check that stock was reduced
    Given path '/stock/' + stockId1
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    And match response contains { quantity: 139.0 }

    # Check that the balance was reduced
    Given path '/balance'
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    Then assert response.balance == 498.7

    # Check that purchase exists
    Given path '/purchase', purchaseId
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    And match response contains { id: '#uuid', createdOn: '#string', totalPrice: '#number', items: '#array'}
    And match each response.items contains { id: '#uuid', name: '#string', amount: '#number', pricePerUnit: '#number', unitType: '#string' }
    And def calculatedPrice = calcPrice(response.items)
    And assert response.totalPrice == calculatedPrice

    # Check that purchase exists in purchase list
    Given path '/purchase'
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    And assert response.purchases.length == 1
    And def purchase = findItemWithId(response.purchases, purchaseId)
    And match purchase contains { id: #(purchaseId) }
    And def purchasedItem1 = findItemWithId(purchase.items, stockId1)
    And match purchasedItem1 contains { id: #(stockId1) }

  Scenario: Purchase multiple items
    # Get token
    Given path 'auth', 'login'
    And request { username: 'orderer',  password: #(password) }
    When method POST
    Then status 200
    And def oToken = response.token

    # Create item 1
    Given path '/stock'
    And header Authorization = "Bearer " + oToken
    And request { name: "Bananas", unitType: "WEIGHT", quantity: 140.0, pricePerUnit: 1.3, stockStatus: 'INSTOCK' }
    When method POST
    Then status 201
    And def stockId1 = response.id

    # Create item 2
    Given path '/stock'
    And header Authorization = "Bearer " + oToken
    And request { name: "Pumpkin", unitType: "PIECE", quantity: 20.0, pricePerUnit: 4.3, stockStatus: 'INSTOCK' }
    When method POST
    Then status 201
    And def stockId2 = response.id

    # Login with member
    Given path 'auth', 'login'
    And request { username: 'member',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    # Set members balance to 500
    Given path 'balance'
    And header Authorization = "Bearer " + token
    And request { balance: 500 }
    When method PATCH
    Then status 200

    # Purchase items
    Given path '/purchase'
    And header Authorization = "Bearer " + token
    And def item1 = { id: #(stockId1), amount: 1 }
    And def item2 = { id: #(stockId2), amount: 1 }
    And request { items: [#(item1), #(item2)] }
    When method POST
    Then status 200
    And match response contains { id: '#uuid', name: '#string', balance: '#number', price: '#number' }
    And assert response.name == "member"
    And assert response.price == 5.6
    And def purchaseId = response.id

    # Check stock was reduced on first item
    Given path '/stock/' + stockId1
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    And match response contains { quantity: 139.0 }

    # Check stock was reduced on second item
    Given path '/stock/' + stockId2
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    And match response contains { quantity: 19.0 }

    # Check that the balance was reduced
    Given path '/balance'
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    Then assert response.balance == 494.4

    # Check that purchase exists
    Given path '/purchase', purchaseId
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    And match response contains { id: '#uuid', createdOn: '#string', totalPrice: '#number', items: '#array'}
    And match each response.items contains { id: '#uuid', name: '#string', amount: '#number', pricePerUnit: '#number', unitType: '#string' }
    And def calculatedPrice = calcPrice(response.items)
    And assert response.totalPrice == calculatedPrice

    # Check that purchase exists in list
    Given path '/purchase'
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    And def purchase = findItemWithId(response.purchases, purchaseId)
    And match purchase contains { id: #(purchaseId) }
    And assert purchase.items.length == 2
    And def purchasedItem1 = findItemWithId(purchase.items, stockId1)
    And match purchasedItem1 contains { id: #(stockId1) }
    And def purchasedItem2 = findItemWithId(purchase.items, stockId2)
    And match purchasedItem2 contains { id: #(stockId2) }

  Scenario: Cannot purchase fractional items when unitType is PIECE
    # Login with orderer to create item
    Given path 'auth', 'login'
    And request { username: 'orderer',  password: #(password) }
    When method POST
    Then status 200
    And def oToken = response.token

    # Create item 1
    Given path '/stock'
    And header Authorization = "Bearer " + oToken
    And request { name: "Bananas", unitType: "WEIGHT", quantity: 140.0, pricePerUnit: 1.3, stockStatus: 'INSTOCK' }
    When method POST
    Then status 201
    And def stockId1 = response.id

    # Create item 2
    Given path '/stock'
    And header Authorization = "Bearer " + oToken
    And request { name: "Pumpkin", unitType: "PIECE", quantity: 20.0, pricePerUnit: 4.3, stockStatus: 'INSTOCK' }
    When method POST
    Then status 201
    And def stockId2 = response.id

    # Login with member
    Given path 'auth', 'login'
    And request { username: 'member',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    # Set members balance to 500
    Given path 'balance'
    And header Authorization = "Bearer " + token
    And request { balance: 500 }
    When method PATCH
    Then status 200

    # Purchase item
    Given path '/purchase'
    And header Authorization = "Bearer " + token
    And def item1 = { id: #(stockId1), amount: 1 }
    And def item2 = { id: #(stockId2), amount: 1.5 }
    And request { items: [#(item1), #(item2)] }
    When method POST
    Then status 400
    And assert response.errorCode == 400008

    # Check quantity didn't change
    Given path '/stock/' + stockId1
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    And match response contains { quantity: 140.0 }

    # Check quantity didn't change
    Given path '/stock/' + stockId2
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    And match response contains { quantity: 20.0 }

    # Check that the balance wasn't reduced
    Given path '/balance'
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    Then assert response.balance == 500

  Scenario: Cannot purchase deleted item
    # Get token for orderer
    Given path 'auth', 'login'
    And request { username: 'orderer',  password: #(password) }
    When method POST
    Then status 200
    And def oToken = response.token

    # Create item 1
    Given path '/stock'
    And header Authorization = "Bearer " + oToken
    And request { name: "Bananas", unitType: "WEIGHT", quantity: 140.0, pricePerUnit: 1.3, stockStatus: 'INSTOCK' }
    When method POST
    Then status 201
    And def stockId1 = response.id

    # Create item 2
    Given path '/stock'
    And header Authorization = "Bearer " + oToken
    And request { name: "Pumpkin", unitType: "PIECE", quantity: 20.0, pricePerUnit: 4.3, stockStatus: 'INSTOCK' }
    When method POST
    Then status 201
    And def stockId2 = response.id

    # Delete item 2
    Given path '/stock/' + stockId2
    And header Authorization = "Bearer " + oToken
    When method DELETE
    Then status 204

    # Login with member
    Given path 'auth', 'login'
    And request { username: 'member',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    # Purchase items
    Given path '/purchase'
    And header Authorization = "Bearer " + token
    And def item1 = { id: #(stockId1), amount: 1 }
    And def item2 = { id: #(stockId2), amount: 1 }
    And request { items: [#(item1), #(item2)] }
    When method POST
    Then status 400
    And assert response.errorCode == 400011

    # Check that the balance wasn't reduced
    Given path '/balance'
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    Then assert response.balance == 500

  Scenario: Purchase a single item with higher amount than in stock is possible
    # Get token of orderer
    Given path 'auth', 'login'
    And request { username: 'orderer',  password: #(password) }
    When method POST
    Then status 200
    And def oToken = response.token

    # Create item 1
    Given path '/stock'
    And header Authorization = "Bearer " + oToken
    And request { name: "Bananas", unitType: "WEIGHT", quantity: 140.0, pricePerUnit: 1.3, stockStatus: 'INSTOCK' }
    When method POST
    Then status 201
    And def stockId1 = response.id

    # Login with member
    Given path 'auth', 'login'
    And request { username: 'member',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    # Set members balance to 500
    Given path 'balance'
    And header Authorization = "Bearer " + token
    And request { balance: 500 }
    When method PATCH
    Then status 200

    # Purchase Items
    Given path '/purchase'
    And header Authorization = "Bearer " + token
    And def item1 = { id: #(stockId1), amount: 200 }
    And request { items: [#(item1)] }
    When method POST
    Then status 200
    And match response contains { id: '#uuid', name: '#string', balance: '#number', price: '#number' }
    And assert response.name == "member"
    And def purchaseId = response.id

    # Check quantity is below 0
    Given path '/stock/' + stockId1
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    And assert response.quantity == -60

    # Check that the balance was reduced
    Given path '/balance'
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    Then assert response.balance < 500

    # Check that purchase exists
    Given path '/purchase', purchaseId
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    And match response contains { id: '#uuid', createdOn: '#string', totalPrice: '#number', items: '#array'}
    And match each response.items contains { id: '#uuid', name: '#string', amount: '#number', pricePerUnit: '#number', unitType: '#string' }
    And def calculatedPrice = calcPrice(response.items)
    And assert response.totalPrice == calculatedPrice

    # Check that purchase exists in list
    Given path '/purchase'
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    And def purchase = findItemWithId(response.purchases, purchaseId)
    And match purchase contains { id: #(purchaseId) }
    And assert purchase.items.length == 1
    And def purchasedItem1 = findItemWithId(purchase.items, stockId1)
    And match purchasedItem1 contains { id: #(stockId1) }

  Scenario: Cannot have multiple items with same id in items array
    # Get token of orderer
    Given path 'auth', 'login'
    And request { username: 'orderer',  password: #(password) }
    When method POST
    Then status 200
    And def oToken = response.token

    # Create item 1
    Given path '/stock'
    And header Authorization = "Bearer " + oToken
    And request { name: "Bananas", unitType: "WEIGHT", quantity: 140.0, pricePerUnit: 1.3, stockStatus: 'INSTOCK' }
    When method POST
    Then status 201
    And def stockId1 = response.id

    # Login with member
    Given path 'auth', 'login'
    And request { username: 'member',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    # Set members balance to 500
    Given path 'balance'
    And header Authorization = "Bearer " + token
    And request { balance: 500 }
    When method PATCH
    Then status 200

    # Purchase  items
    Given path '/purchase'
    And header Authorization = "Bearer " + token
    And def item1 = { id: #(stockId1), amount: 1 }
    And def item1AsWell = { id: #(stockId1), amount: 5 }
    And request { items: [#(item1), #(item1AsWell)] }
    When method POST
    Then status 400
    And assert response.errorCode == 400010

    # Check that the balance wasn't reduced
    Given path '/balance'
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    Then assert response.balance == 500

  Scenario: The price of a purchase does not change after a stock items price was updated
    # Get token orderer
    Given path 'auth', 'login'
    And request { username: 'orderer',  password: #(password) }
    When method POST
    Then status 200
    And def oToken = response.token

    # Create item
    Given path '/stock'
    And header Authorization = "Bearer " + oToken
    And request { name: "Bananas", unitType: "WEIGHT", quantity: 140.0, pricePerUnit: 1.0, stockStatus: 'INSTOCK' }
    When method POST
    Then status 201
    And def stockId1 = response.id

    # Login with member
    Given path 'auth', 'login'
    And request { username: 'member',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    # Purchase item
    Given path '/purchase'
    And header Authorization = "Bearer " + token
    And def item1 = { id: #(stockId1), amount: 1 }
    And request { items: [#(item1)] }
    When method POST
    Then status 200
    And match response contains { id: '#uuid', name: '#string', balance: '#number', price: '#number' }
    And def purchaseId = response.id

    # Get the purchase
    Given path '/purchase', purchaseId
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    And match response contains { id: '#uuid', createdOn: '#string', totalPrice: '#number', items: '#array'}
    And match each response.items contains { id: '#uuid', name: '#string', amount: '#number', pricePerUnit: '#number', unitType: '#string' }
    And def totalPriceFirstTime = response.totalPrice
    And def calculatedPriceFirstTime = calcPrice(response.items)

    # Update price of an item
    Given path '/stock/' + stockId1
    And header Authorization = "Bearer " + oToken
    And request { pricePerUnit: 5.0 }
    When method PATCH
    Then status 200

    # Get the purchase a second time
    Given path '/purchase', purchaseId
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    And match response contains { id: '#uuid', createdOn: '#string', totalPrice: '#number', items: '#array'}
    And match each response.items contains { id: '#uuid', name: '#string', amount: '#number', pricePerUnit: '#number', unitType: '#string' }
    And def totalPriceSecondTime = response.totalPrice
    And def calculatedPriceSecondTime = calcPrice(response.items)

    # Check prices are still the same
    And assert totalPriceFirstTime == totalPriceSecondTime
    And assert calculatedPriceFirstTime == calculatedPriceSecondTime

  Scenario: Request purchase via id from a different user fails
    # Get token of orderer
    Given path 'auth', 'login'
    And request { username: 'orderer',  password: #(password) }
    When method POST
    Then status 200
    And def oToken = response.token

    # Get token of member
    Given path 'auth', 'login'
    And request { username: 'member',  password: #(password) }
    When method POST
    Then status 200
    And def mToken = response.token

    # Create item
    Given path '/stock'
    And header Authorization = "Bearer " + oToken
    And request { name: "Bananas", unitType: "WEIGHT", quantity: 140.0, pricePerUnit: 1.3, stockStatus: 'INSTOCK' }
    When method POST
    Then status 201
    And def stockId1 = response.id

    # Orderer purchases item
    Given path '/purchase'
    And header Authorization = "Bearer " + oToken
    And def item1 = { id: #(stockId1), amount: 1 }
    And request { items: [#(item1)] }
    When method POST
    Then status 200
    And match response contains { id: '#uuid', name: '#string', balance: '#number', price: '#number' }
    And assert response.name == "orderer"
    And assert response.price == 1.3
    And def purchaseId = response.id

    # Request purchase from different user
    Given path '/purchase', purchaseId
    And header Authorization = "Bearer " + mToken
    When method GET
    Then status 401
    And assert response.errorCode == 401001

  Scenario: GET /purchase requires authorization
    Given path '/purchase'
    When method GET
    Then status 401
    And assert response.errorCode == 401005

  Scenario: POST /purchase requires authorization
    Given path '/purchase'
    And def item1 = { id: "13246", amount: 1 }
    And request { items: [#(item1)] }
    When method POST
    Then status 401
    And assert response.errorCode == 401005

  Scenario: GET /purchase/{purchaseId} requires authorization
    Given path '/purchase', "132456"
    When method GET
    Then status 401
    And assert response.errorCode == 401005

  Scenario: Purchasing an item with stockStatus OUTOFSTOCK is not possible
    # Create Item with Orderer
    Given path 'auth', 'login'
    And request { username: 'orderer',  password: #(password) }
    When method POST
    Then status 200
    And def oToken = response.token

    # Create item that is out of stock
    Given path '/stock'
    And header Authorization = "Bearer " + oToken
    And def quantityOriginal = 100.0
    And request { name: "Bananas", unitType: "WEIGHT", quantity: #(quantityOriginal), pricePerUnit: 1.3, stockStatus: 'OUTOFSTOCK' }
    When method POST
    Then status 201
    And def stockId1 = response.id

    # Create item that is out of stock
    Given path '/stock'
    And header Authorization = "Bearer " + oToken
    And def quantityOriginal = 100.0
    And request { name: "Bananas", unitType: "WEIGHT", quantity: #(quantityOriginal), pricePerUnit: 1.3, stockStatus: 'INSTOCK' }
    When method POST
    Then status 201
    And def stockId2 = response.id

    # Login with member
    Given path 'auth', 'login'
    And request { username: 'member',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    # Purchase item
    Given path '/purchase'
    And header Authorization = "Bearer " + token
    And def item1 = { id: #(stockId1), amount: 1 }
    And def item2 = { id: #(stockId2), amount: 1 }
    And request { items: [#(item1), #(item2)] }
    When method POST
    Then status 400
    And match response.errorCode == 400018

    # Check that item's quantity did not change
    Given path 'stock', stockId1
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    And match response.quantity == quantityOriginal

    # Check that item's quantity did not change
    Given path 'stock', stockId2
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    And match response.quantity == quantityOriginal

  Scenario: Purchasing an item with stockStatus ORDERED is not possible
    # Create Item with Orderer
    Given path 'auth', 'login'
    And request { username: 'orderer',  password: #(password) }
    When method POST
    Then status 200
    And def oToken = response.token

    # Create item that is out of stock
    Given path '/stock'
    And header Authorization = "Bearer " + oToken
    And def quantityOriginal = 100.0
    And request { name: "Bananas", unitType: "WEIGHT", quantity: #(quantityOriginal), pricePerUnit: 1.3, stockStatus: 'ORDERED' }
    When method POST
    Then status 201
    And def stockId1 = response.id

    # Create item that is out of stock
    Given path '/stock'
    And header Authorization = "Bearer " + oToken
    And def quantityOriginal = 100.0
    And request { name: "Bananas", unitType: "WEIGHT", quantity: #(quantityOriginal), pricePerUnit: 1.3, stockStatus: 'INSTOCK' }
    When method POST
    Then status 201
    And def stockId2 = response.id

    # Login with member
    Given path 'auth', 'login'
    And request { username: 'member',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    # Purchase item
    Given path '/purchase'
    And header Authorization = "Bearer " + token
    And def item1 = { id: #(stockId1), amount: 1 }
    And def item2 = { id: #(stockId2), amount: 1 }
    And request { items: [#(item1), #(item2)] }
    When method POST
    Then status 400
    And match response.errorCode == 400019

    # Check that item's quantity did not change
    Given path 'stock', stockId1
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    And match response.quantity == quantityOriginal

    # Check that item's quantity did not change
    Given path 'stock', stockId2
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    And match response.quantity == quantityOriginal
