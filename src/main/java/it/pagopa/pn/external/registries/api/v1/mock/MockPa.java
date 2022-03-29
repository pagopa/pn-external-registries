package it.pagopa.pn.external.registries.api.v1.mock;

public class MockPa {
    private String id;

    private String name;

    private String taxId;

    private MockPaContacts generalContacts;

    public String getId() {
        return id;
    }

    public MockPa() {
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public MockPaContacts getGeneralContacts() {
        return generalContacts;
    }

    public void setGeneralContacts(MockPaContacts generalContacts) {
        this.generalContacts = generalContacts;
    }

}
