## Using the BitPay Java light client

This SDK provides a convenient abstraction of BitPay's [cryptographically-secure API](https://bitpay.com/api) and allows payment gateway developers to focus on payment flow/e-commerce integration rather than on the specific details of client-server interaction using the API.  This SDK optionally provides the flexibility for developers to have control over important details, including the handling of private tokens needed for client-server communication.

### Dependencies

You must have a BitPay merchant account to use this SDK.  It's free to [sign-up for a BitPay merchant account](https://bitpay.com/start).

### Getting your client token

First of all, you need to generate a new POS token on your BitPay's account which will be required to securely connect to the BitPay's API.  
For testing purposes use:  
https://test.bitpay.com/dashboard/merchant/api-tokens

For production use:  
https://bitpay.com/dashboard/merchant/api-tokens

Click on 'Add New Token', give a name on the Token Label input, leave the 'Require Authentication' checkbox unchecked and click on 'Add Token'.
The new token will appear and ready to use.


### Initializing your BitPay client

Once you have the token, you can initialize the client for the desired environment:

```java
// Testing
import com.bitpay.sdk_light.Client;
import com.bitpay.sdk_light.Env;

Client bitpay = new Client("H78Yiu78uh78Gjht6g67gjh6767ghj", Env.Test);
```

```java
// Production [The environment is selected by default]
import com.bitpay.sdk_light.Client;

Client bitpay = new Client("uh78Gjht6g67gjH78Yiu78h6767ghj");
```

### Create an invoice

```java
Invoice invoice = bitpay.createInvoice(new Invoice(100.0, "USD"));

String invoiceUrl = invoice.Url;

String status = invoice.Status;
```

### Create an invoice (extended)

You can add optional attributes to the invoice.  Atributes that are not set are ignored or given default values.
```java
Buyer buyerData = new Buyer();
buyerData.setName("Satoshi");
buyerData.setAddress1("street");
buyerData.setAddress2("911");
buyerData.setLocality("Washington");
buyerData.setRegion("District of Columbia");
buyerData.setPostalCode("20000");
buyerData.setCountry("USA");
buyerData.setEmail("satoshi@buyeremaildomain.com");
buyerData.setNotify(true);

Invoice invoice = new Invoice(100.0, Currency.USD)
invoice.setBuyer(buyerData);

invoice = bitpay.createInvoice(invoice);
```

### Retreive an invoice

```java
Invoice invoice = bitpay.getInvoice(invoice.getId());
```

### Get exchange rates

You can retrieve BitPay's [BBB exchange rates](https://bitpay.com/exchange-rates).

```java
Rates rates = bitpay.getRates();

Double rate = rates.getRate("USD");

rates.update();
```

### Create a bill

```java
// Create a list of items to add in the bill
List<Item> items = new ArrayList<>();
items.add(new Item() {{
    setPrice(30.0);
    setQuantity(9);
    setDescription("product-a");
}});
items.add(new Item() {{
    setPrice(14.0);
    setQuantity(16);
    setDescription("product-b");
}});

Bill bill = new Bill("1111", Currency.USD, "email@merchantsemail.com", items);

bill = bitpay.createBill(bill);
```

### Retreive a bill

```java
Bill bill = bitpay.getBill(bill.Id);
```

### Deliver a bill

```java
String deliveryResult = bitpay.deliverBill(bill.getId(), bill.getToken());
```


See also the tests project for more examples of API calls.