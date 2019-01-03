app.controller('searchController',function ($scope,$location,searchService) {
    //定义搜索对象的结构category:商品分类
    $scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':40,'sortField':'','sort':''};



    //搜索
    $scope.search = function () {
        $scope.searchMap.pageNo= parseInt($scope.searchMap.pageNo) ;//执行查询前，转换为int类型，否则提交到后端有可能变成字符串
        searchService.search($scope.searchMap).success(
            function (response) {
                $scope.resultMap = response;//搜所返回结果
                buildPageLabel();//调用
            }
        )
    }

    $scope.addSearchItem=function (key, value) {
        if(key=='category' || key=='brand' || key=='price'){//如果用户点击的是分类或品牌
            $scope.searchMap[key]=value;
        }else{//用户点击的是规格
            $scope.searchMap.spec[key]=value;

        }
        $scope.search();
    }

    //撤销搜索条件
    $scope.removeSearchItem=function(key){
        if(key=="category" ||  key=="brand" || key=="price"){//如果是分类或品牌
            $scope.searchMap[key]="";
        }else{//否则是规格
            delete $scope.searchMap.spec[key];//移除此属性
        }

        $scope.search();
    }

    //构建分页标签（totalPage为总数）
    buildPageLabel=function () {
    $scope.pageLabel=[];//新增分页栏属性
    var maxPageNo = $scope.resultMap.totalPages;//得到最后页码
    var firstPage=1;//开始页码
    var lastPage=maxPageNo;//截至页码
    if($scope.resultMap.totalPages>5){  //如果总页数大于5页,显示部分页码
        if($scope.searchMap.pageNo<=3){//如果当前页小于等于3
            lastPage=5;//前5页
            }else if($scope.searchMap.pageNo>=lastPage-2){//如果当前页大于等于最大页码-2
            firstPage=maxPageNo-4; //后5页
            }else{ //显示当前页为中心的5页
            firstPage=$scope.searchMap.pageNo-2;
            lastPage=$scope.searchMap.pageNo+2;
            }

         }else{
        firstPage=1;
        lastPage=maxPageNo;
         }


    //循环产生页码标签
    for(var i=firstPage;i<=lastPage;i++){
        $scope.pageLabel.push(i);
              }
    }



    //根据页码查询
    $scope.queryByPage=function (pageNo) {
        //页码验证
        if(pageNo<1 || pageNo>$scope.resultMap.totalPages){
            return;
        }
        $scope.searchMap.pageNo=pageNo;
        $scope.search();
    }

    //设置排序规则
    $scope.sortSearch=function (sortField, sort) {
        $scope.searchMap.sortField=sortField;
        $scope.searchMap.sort=sort;
        $scope.search();
    }



  /*  //判断当前页是否未最后一页，在前台判断了s
    $scope.isEndPage=function(){
        if($scope.searchMap.pageNo==$scope.resultMap.totalPages){
            return true;
        }else{
            return false;
        }
    }*/



    //判断关键字是不是品牌
    $scope.keywordsIsBrand=function(){
        for(var i=0;i<$scope.resultMap.brandList.length;i++){
            if($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text)>=0){//如果包含
                return true;
            }
        }
        return false;
    };

    //加载关键字
    $scope.loadkeywords=function(){
        $scope.searchMap.keywords=  $location.search()['keywords'];
        $scope.search();
    }





});