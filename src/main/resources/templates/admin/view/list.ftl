<#include "/admin/utils/ui.ftl"/>
<@layout>
    <section class="content-header">
        <h1>浏览管理</h1>
        <ol class="breadcrumb">
            <li><a href="${base}/admin">首页</a></li>
            <li class="active">浏览管理</li>
        </ol>
    </section>
    <section class="content container-fluid">
        <div class="row">
            <div class="col-md-12">
                <div class="box">
                    <div class="box-header with-border">
                        <h3 class="box-title">浏览列表</h3>
                    </div>
                    <div class="box-body">
                        <div class="table-responsive">
                            <table id="dataGrid" class="table table-striped table-bordered">
                                <thead>
                                <tr>
                                    <th>浏览时间</th>
                                    <th>浏览地址</th>
                                    <th>页面来源</th>
                                    <th>IP</th>
                                    <th>归属地</th>
                                    <th>浏览器</th>
                                    <th>设备</th>
                                    <th>系统</th>
                                </tr>
                                </thead>
                                <tbody>
                                <#list page.content as row>
                                    <tr>
                                        <td>${row.createTime}</td>
                                        <td>${row.url}</td>
                                        <td>${row.referrer}</td>
                                        <td>${row.ip}</td>
                                        <td>${row.location}</td>
                                        <td>${row.browserName}</td>
                                        <td>${row.deviceType}</td>
                                        <td>${row.operatingSystem}</td>
                                    </tr>
                                </#list>
                                </tbody>
                            </table>
                        </div>
                    </div>
                    <div class="box-footer">
                        <@pager "list" page 5 />
                    </div>
                </div>
            </div>
        </div>
    </section>
</@layout>
