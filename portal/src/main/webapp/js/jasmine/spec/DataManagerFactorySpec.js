describe("DataManagerFactory", function() {
    it("DataManagers should subscribe and fire", function() {

        var dataManager = DataManagerFactory.getNewDataManager();

        var subscriber = jasmine.createSpy();
        dataManager.subscribe(subscriber);
        expect(subscriber).not.toHaveBeenCalled();

        dataManager.fire({});

        expect(subscriber).toHaveBeenCalled();
    });

    it("should always have a DataManager for Genes (.getGeneDataManager)", function() {
        var geneDataManager = DataManagerFactory.getGeneDataManager();

        expect(geneDataManager).not.toBe(undefined);
        expect(geneDataManager.fire).not.toBe(undefined);
        expect(geneDataManager.subscribe).not.toBe(undefined);
    });

    it("should have a url for gene data json", function() {
        expect(DataManagerFactory.getGeneDataJsonUrl()).not.toBe(undefined);
    });
});
