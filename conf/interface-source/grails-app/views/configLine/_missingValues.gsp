<%--
  Created by IntelliJ IDEA.
  User: Luis Alcantara
  Date: 2019-05-03
  Time: 13:53
--%>
<%@ page import="switchboard.cloud.ConfigLine" contentType="text/html;charset=UTF-8" %>
<g:set var="configLines" value="${ConfigLine.findAllByValue(ConfigLine.MISSING_VALUE)}" />
<g:if test="${configLines}">
    <h4>Uninitialized Configuration Properties</h4>
    <ul class="list-group">
        <g:each in="${configLines}" var="configLine" >
            <li class="list-group-item row" ondblclick="selectProperty('${configLine.id}')" >
                <p class="col-xs-2" >Category: </p><p class="col-xs-4 config-line-category" >${configLine.category}</p>
                <p class="col-xs-2" >Property Name:</p><p class="col-xs-4 config-line-property" >${configLine.property}</p>
            </li>
        </g:each>
    </ul>
</g:if>