<#macro link text href>
    <@_button href=href>${text}</@_button>
</#macro>

<#macro iconLink text href direction="right" additionalAttributes="">
    <@_button href=href isInline=true isUppercased=false type="clear" additionalAttributes=additionalAttributes>
        <#if direction=='left'>
            <i class="icon icon-gizmo-navigate-left"></i>
        </#if>
        <span>${text}</span>
        <#if direction=='right'>
            <i class="icon icon-gizmo-navigate-right"></i>
        </#if>
    </@_button>
</#macro>

<#macro _button href isInline=false isUppercased=true type="info" additionalAttributes="">
<div class="button-wrapper ${isInline?then('inline', '')}">
    <a class="button ${isUppercased?then('','original-case')} ${type}" href="${href}" ${additionalAttributes}>
        <#nested />
    </a>
</div>
</#macro>