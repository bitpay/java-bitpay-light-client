package tests;

import com.bitpay.sdk_light.exceptions.BitPayException;
import com.bitpay.sdk_light.Client;
import com.bitpay.sdk_light.Env;
import com.bitpay.sdk_light.model.Bill.*;
import com.bitpay.sdk_light.model.Currency;
import com.bitpay.sdk_light.model.Invoice.*;
import com.bitpay.sdk_light.model.Rate.*;
import com.bitpay.sdk_light.util.BitPayLogger;
import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class BitPayTest {

    private Client _bitpay;
    private final static String _environment = Env.Test;
    private final static String _token = "Hn2WCJfDcVKT5kzS8YST33WghmUV3n7WDRKh5mhk4bW6";
    private final static double EPSILON = .001;
    private final static double BTC_EPSILON = .000000001;
    private Invoice basicInvoice;

    @Before
    public void setUp() throws BitPayException {
        // Initialize the BitPay object to be used in the following tests
        _bitpay = new Client(_token, _environment);
        _bitpay.setLoggerLevel(BitPayLogger.DEBUG);
    }

    @Test
    public void testShouldGetInvoiceId() {
        // create an invoice and make sure we receive an id - which means it has been successfully submitted
        Invoice invoice = new Invoice(30.0, Currency.USD);
        Invoice basicInvoice = null;
        try {
            basicInvoice = _bitpay.createInvoice(invoice);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertNotNull(basicInvoice.getId());
    }

    @Test
    public void testShouldCreateInvoiceBTC() {
        Invoice invoice = new Invoice(50.0, "USD");
        invoice.setPaymentCurrencies(Arrays.asList(Currency.BTC));
        try {
            basicInvoice = _bitpay.createInvoice(invoice);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertNotNull(basicInvoice.getId());
    }

    @Test
    public void testShouldCreateInvoiceBCH() {
        Invoice invoice = new Invoice(50.0, "USD");
        invoice.setPaymentCurrencies(Arrays.asList(Currency.BCH));
        try {
            basicInvoice = _bitpay.createInvoice(invoice);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertNotNull(basicInvoice.getId());
    }

    @Test
    public void testShouldCreateInvoiceETH() {
        Invoice invoice = new Invoice(50.0, "USD");
        invoice.setPaymentCurrencies(Arrays.asList(Currency.ETH));
        try {
            basicInvoice = _bitpay.createInvoice(invoice);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertNotNull(basicInvoice.getId());
    }

    @Test
    public void testShouldGetInvoiceURL() {
        // create an invoice and make sure we receive an invoice url - which means we can check it online
        Invoice invoice = new Invoice(10.0, Currency.USD);
        Invoice basicInvoice = null;
        try {
            basicInvoice = _bitpay.createInvoice(invoice);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertNotNull(basicInvoice.getUrl());
    }

    @Test
    public void testShouldGetInvoiceStatus() {
        // create an invoice and make sure we receive a correct invoice status (new)
        Invoice invoice = new Invoice(10.0, Currency.USD);
        Invoice basicInvoice = null;
        try {
            basicInvoice = _bitpay.createInvoice(invoice);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertEquals(InvoiceStatus.New, basicInvoice.getStatus());
    }

    @Test
    public void testShouldCreateInvoiceOneTenthBtc() {
        // create an invoice and make sure we receive the correct price value back (under 1 BTC)
        Invoice invoice = new Invoice(0.1, Currency.BTC);
        Invoice basicInvoice = null;
        try {
            invoice = this._bitpay.createInvoice(invoice);
            basicInvoice = this._bitpay.getInvoice(invoice.getId());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertEquals(0.1, basicInvoice.getPrice(), BTC_EPSILON);
    }


    @Test
    public void testShouldCreateInvoice100Usd() {
        // create an invoice and make sure we receive the correct price value back (USD)
        Invoice invoice = new Invoice(100.0, Currency.USD);
        Invoice basicInvoice = null;
        try {
            invoice = this._bitpay.createInvoice(invoice);
            basicInvoice = this._bitpay.getInvoice(invoice.getId());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertEquals(100.0, basicInvoice.getPrice(), EPSILON);
    }


    @Test
    public void testShouldCreateInvoice100Eur() {
        // create an invoice and make sure we receive the correct price value back (EUR)
        Invoice invoice = new Invoice(100.0, Currency.EUR);
        Invoice basicInvoice = null;
        try {
            invoice = this._bitpay.createInvoice(invoice);
            basicInvoice = this._bitpay.getInvoice(invoice.getId());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertEquals(100.0, basicInvoice.getPrice(), EPSILON);
    }


    @Test
    public void testShouldGetInvoice() {
        // create an invoice then retrieve it through the get method - they should match
        Invoice invoice = new Invoice(100.0, Currency.EUR);
        Invoice basicInvoice = null;
        Invoice retrievedInvoice = null;
        try {
            invoice = this._bitpay.createInvoice(invoice);
            basicInvoice = this._bitpay.getInvoice(invoice.getId());
            retrievedInvoice = this._bitpay.getInvoice(basicInvoice.getId());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertEquals(basicInvoice.getId(), retrievedInvoice.getId());
    }

    @Test
    public void testShouldCreateInvoiceWithAdditionalParams() {
        // create an invoice and make sure we receive the correct fields values back
        Buyer buyerData = new Buyer();
        buyerData.setName("Satoshi");
        buyerData.setAddress1("street");
        buyerData.setAddress2("911");
        buyerData.setLocality("Washington");
        buyerData.setRegion("District of Columbia");
        buyerData.setPostalCode("20000");
        buyerData.setCountry("USA");
        buyerData.setEmail("satoshi@buyeremaildomain.com");
//        buyerData.setPhone("");
        buyerData.setNotify(true);

        Invoice invoice = new Invoice(100.0, Currency.USD);
        invoice.setBuyer(buyerData);
        invoice.setFullNotifications(true);
        invoice.setNotificationEmail("satoshi@merchantemaildomain.com");
        invoice.setPosData("ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890");
        try {
            invoice = this._bitpay.createInvoice(invoice);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertEquals(InvoiceStatus.New, invoice.getStatus());
        assertEquals(100.0, invoice.getPrice(), EPSILON);
        assertEquals("ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890", invoice.getPosData());
        assertEquals("Satoshi", invoice.getBuyer().getName());
        assertEquals("satoshi@buyeremaildomain.com", invoice.getBuyer().getEmail());
        assertTrue(invoice.getFullNotifications());
        assertEquals("satoshi@merchantemaildomain.com", invoice.getNotificationEmail());
    }

    @Test
    public void testShouldGetExchangeRates() {
        Rates rates;
        List<Rate> rateList = null;
        try {
            rates = this._bitpay.getRates();
            rateList = rates.getRates();
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertNotNull(rateList);
    }

    @Test
    public void testShouldGetEURExchangeRate() {
        Rates rates;
        double rate = 0.0;
        try {
            rates = this._bitpay.getRates();
            rate = rates.getRate("EUR");
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertTrue(rate != 0);
    }

    @Test
    public void testShouldGetCNYExchangeRate() {
        Rates rates;
        double rate = 0.0;
        try {
            rates = this._bitpay.getRates();
            rate = rates.getRate("CNY");
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertTrue(rate != 0);
    }

    @Test
    public void testShouldUpdateExchangeRates() {
        Rates rates;
        List<Rate> rateList = null;
        try {
            rates = this._bitpay.getRates();
            rates.update();
            rateList = rates.getRates();
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertNotNull(rateList);
    }

    @Test
    public void testShouldGetInvoiceIdOne() {
        // create an invoice and get it by its id
        Invoice invoice = new Invoice(1.0, Currency.USD);
        Invoice basicInvoice;
        Invoice retrievedInvoice = null;
        try {
            invoice = this._bitpay.createInvoice(invoice);
            basicInvoice = this._bitpay.getInvoice(invoice.getId());
            retrievedInvoice = this._bitpay.getInvoice(basicInvoice.getId());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertNotNull(retrievedInvoice.getId());
    }

    @Test
    public void testShouldCreateBillUSD() {
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
        items.add(new Item() {{
            setPrice(3.90);
            setQuantity(42);
            setDescription("product-c");
        }});
        items.add(new Item() {{
            setPrice(6.99);
            setQuantity(12);
            setDescription("product-d");
        }});

        Bill bill = new Bill("7", Currency.USD, "satoshi@merchantemaildomain.com", items);
        Bill basicBill = null;
        try {
            basicBill = this._bitpay.createBill(bill);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertNotNull(basicBill.getId());
    }

    @Test
    public void TestShouldCreateBillEUR() {
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
        items.add(new Item() {{
            setPrice(3.90);
            setQuantity(42);
            setDescription("product-c");
        }});
        items.add(new Item() {{
            setPrice(6.99);
            setQuantity(12);
            setDescription("product-d");
        }});

        Bill bill = new Bill("7", Currency.EUR, "satoshi@merchantemaildomain.com", items);
        Bill basicBill = null;
        try {
            basicBill = this._bitpay.createBill(bill);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertNotNull(basicBill.getId());
    }

    @Test
    public void TestShouldGetBillUrl() {
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
        items.add(new Item() {{
            setPrice(3.90);
            setQuantity(42);
            setDescription("product-c");
        }});
        items.add(new Item() {{
            setPrice(6.99);
            setQuantity(12);
            setDescription("product-d");
        }});

        Bill bill = new Bill("7", Currency.USD, "satoshi@merchantemaildomain.com", items);
        Bill basicBill = null;
        try {
            basicBill = this._bitpay.createBill(bill);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertNotNull(basicBill.getUrl());
    }

    @Test
    public void TestShouldGetBillStatus() {
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
        items.add(new Item() {{
            setPrice(3.90);
            setQuantity(42);
            setDescription("product-c");
        }});
        items.add(new Item() {{
            setPrice(6.99);
            setQuantity(12);
            setDescription("product-d");
        }});

        Bill bill = new Bill("7", Currency.USD, "satoshi@merchantemaildomain.com", items);
        Bill basicBill = null;
        try {
            basicBill = this._bitpay.createBill(bill);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertEquals(BillStatus.Draft, basicBill.getStatus());
    }


    @Test
    public void testShouldGetBillTotals() {
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
        items.add(new Item() {{
            setPrice(3.90);
            setQuantity(42);
            setDescription("product-c");
        }});
        items.add(new Item() {{
            setPrice(6.99);
            setQuantity(12);
            setDescription("product-d");
        }});

        Bill bill = new Bill("5", Currency.USD, "satoshi@merchantemaildomain.com", items);
        Bill basicBill = null;
        try {
            basicBill = this._bitpay.createBill(bill);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertEquals(bill.getItems().stream().mapToDouble(Item::getPrice).sum(),basicBill.getItems().stream().mapToDouble(Item::getPrice).sum(), EPSILON);
    }

    @Test
    public void TestShouldGetBill() {
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
        items.add(new Item() {{
            setPrice(3.90);
            setQuantity(42);
            setDescription("product-c");
        }});
        items.add(new Item() {{
            setPrice(6.99);
            setQuantity(12);
            setDescription("product-d");
        }});

        Bill bill = new Bill("7", Currency.USD, "satoshi@merchantemaildomain.com", items);
        Bill basicBill = null;
        Bill retrievedBill = null;
        try {
            basicBill = this._bitpay.createBill(bill);
            retrievedBill = this._bitpay.getBill(basicBill.getId());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertEquals(basicBill.getId(), retrievedBill.getId());
    }

    @Test
    public void TestShouldDeliverBill() {
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
        items.add(new Item() {{
            setPrice(3.90);
            setQuantity(42);
            setDescription("product-c");
        }});
        items.add(new Item() {{
            setPrice(6.99);
            setQuantity(12);
            setDescription("product-d");
        }});

        Bill bill = new Bill("7", Currency.USD, "satoshi@merchantemaildomain.com", items);
        Bill basicBill;
        String result = "";
        try {
            basicBill = this._bitpay.createBill(bill);
            result = this._bitpay.deliverBill(basicBill.getId(), basicBill.getToken());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertEquals("Success", result);
    }
}
