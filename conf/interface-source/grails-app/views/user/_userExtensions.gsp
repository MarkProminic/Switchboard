<g:select from="${user.extensions}" name="user-location-list" class="form-control"
          optionValue="displayName" optionKey="id" value="${user.primaryExtension.id}" onchange="changeLocation()" />