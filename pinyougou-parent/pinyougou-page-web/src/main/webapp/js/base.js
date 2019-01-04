var app=angular.module('pinyougou',[]);
/*写angularjs的过滤器*/
app.filter('trustHtml',['$sce',function ($sce) {
    return function (data) {/*闯入参数是被过滤内容*/
        return $sce.trustAsHtml(data);    /*返回的是过滤后的内容*/
    }
}]);