<#-- @ftlvariable name="allowGuestUser" type="Boolean" -->
<#-- @ftlvariable name="appEmail" type="String" -->
<#-- @ftlvariable name="appName" type="String" -->
<#-- @ftlvariable name="appUrl" type="String" -->
<@layout>
    <h1>Accessibility on ${appName}</h1>
    <p id="access-loc">This statement applies to content published on ${appUrl}</p>
    <p>We want as many people as possible to be able to use this website. For example, that means you should
        be able to:</p>
    <ul>
        <li>Resize your window with content being reformatted appropriately</li>
        <li>Adjust your text size without the site becoming less usable</li>
        <li>Read text easily due to sufficient contrast between foreground and background elements</li>
    </ul>
    <p><a href="https://mcmw.abilitynet.org.uk/">AbilityNet</a> has advice on making your device easier to
        use if you have a disability.</p>
    <h2>How accessible the website is</h2>
    <p>Parts of this website may not be fully accessible. For example:</p>
    <ul>
        <li>Some form elements may lack descriptive names or labels</li>
        <li>Some elements may lack sufficient colour contrast with their background</li>
    </ul>
    <h2>What we do about known issues</h2>
    <p>We work to achieve and maintain <a href="https://www.w3.org/TR/WCAG21/">WCAG 2.1 AA standards</a>,
        but it is not always possible for all our
        content to be accessible. Where content is not accessible, we will state a reason, warn users and
        offer alternatives.</p>
    <#if allowGuestUser>
        <div id="access-regs">
            <h2>Technical information about this website’s accessibility</h2>
            <p>We are committed to making this website accessible in accordance with the Public
                Sector Bodies (Websites and Mobile Applications) (No. 2) Accessibility Regulations 2018.</p>
            <p>This website is partially compliant with the <a href="https://www.w3.org/TR/WCAG21/">Web Content
                    Accessibility Guidelines version 2.1</a> AA standard, due to the known issues listed above.</p>
        </div>
    </#if>
    <h2>Reporting accessibility issues</h2>
    <p id="access-email">If you need information on this website in a different format like accessible PDF, large print, easy
        read, audio recording or braille or if you find any accessibility issues not listed on this page
        then please contact <a href="mailto:${appEmail}">${appEmail}</a>.
    </p>
    <p>We’ll consider your request and get back to you in 7 days.</p>
    <#if allowGuestUser>
        <div id="access-enforce">
            <h2>Enforcement procedure</h2>
            <p>The Equality and Human Rights Commission (EHRC) is responsible for enforcing the Public Sector Bodies
                (Websites and Mobile Applications) (No. 2) Accessibility Regulations 2018 (the ‘accessibility
                regulations’). If you’re not happy with how we respond to your complaint, <a
                        href="https://www.equalityadvisoryservice.com/">contact the Equality Advisory and Support
                    Service (EASS)</a>.</p>
        </div>
    </#if>
    <h2>How we test this website</h2>
    <p>This website was last tested for accessibility compliance on 25 March 2021, and these tests have
        been carried out internally using the <a href="https://accessibilityinsights.io/en/">Accessibility
            Insights</a> tools.</p>
    <h2>Last updated</h2>
    <p>This statement was prepared on 25 March 2021. It was last updated on 25 March 2021.</p>
</@layout>
