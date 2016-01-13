'use strict';

(function (angular) {

  var FeatureCoreService = function (FeatureResource) {

    this.getFeaturesByTestRunId = function (testRunId) {
      return FeatureResource.query({ testRunId: testRunId }).$promise;
    };

    this.getById = function (featureId) {
      return FeatureResource.get({ featureId: featureId }).$promise;
    };

    this.getStats = function (featureId) {
      return FeatureResource.getStats({ featureId: featureId }).$promise;
    };

    this.delete = function (featureId) {
      return FeatureResource.delete({ featureId: featureId}).$promise;
    };

    this.getFeatureHistory = function (featureId) {
      return FeatureResource.getFeatureHistory({ featureId: featureId }).$promise;
    };

  };


  angular.module('testsCucumberApp')
    .controller('FeatureCtrl', function ($routeParams, $q, $location, FeatureCoreService, TestRunCoreService, ScenarioCoreService) {

      this.load = function () {

        // Load feature

        FeatureCoreService.getById($routeParams.featureId)
          .then(function (feature) {

            var testRunQ = TestRunCoreService.getById(feature.testRunId);
            var scenariiQ = ScenarioCoreService.getScenariiByFeatureId(feature.id);
            var statsQ = FeatureCoreService.getStats(feature.id);

            return $q.all([testRunQ, scenariiQ, statsQ])
              .then(_.spread(function (testRun, scenarii, stats) {
                feature.testRun = testRun;
                feature.scenarii = scenarii;
                feature.stats = stats;
                return feature;
              }));

          })
          .then(function (feature) {
            this.feature = feature;
          }.bind(this));

          // Load history

          FeatureCoreService.getFeatureHistory($routeParams.featureId)
            .then(function (history) {

              return $q.all(history.map(function (feature) {
                return TestRunCoreService.getById(feature.testRunId)
                  .then(function (testRun) {
                    feature.testRun = testRun;
                    return feature;
                  });
              }));

            })
            .then(function (history) {

              return $q.all(history.map(function (feature) {
                return FeatureCoreService.getStats(feature.id)
                  .then(function (stats) {
                    feature.stats = stats;
                    return feature;
                  });
              }));

            })
            .then(function (history) {
              this.history = history;
            }.bind(this));

      };

      this.delete = function () {
        FeatureCoreService.delete(this.feature.id).then(function () {
          $location.path('/test-runs/' + this.feature.testRun.id);
        }.bind(this));
      };

      this.load();

    })
    .service('FeatureCoreService', FeatureCoreService)
    .service('FeatureResource', function ($resource, baseUri) {
      return $resource(
        baseUri + '/features/:featureId',
        { featureId: '@id' },
        {
          getStats: {
            method: 'GET',
            url: baseUri + '/features/:featureId/stats',
          },
          getFeatureHistory: {
            method: 'GET',
            url: baseUri + '/features/:featureId/history',
             isArray: true,
          }
        }
      );
    })
    .config(function ($routeProvider) {
      $routeProvider
        .when('/features/:featureId', {
          templateUrl: 'views/feature.html',
          controller: 'FeatureCtrl',
          controllerAs: 'ctrl'
        });
    });


})(angular);
