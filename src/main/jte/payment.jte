@param org.springframework.security.web.csrf.CsrfToken _csrf
@param fr.uge.booqin.app.controller.mvc.session.SessionData session = null
@param String nonce

@param int amount
@param String stripePublicKey
@param String currency


@template.layout.base(
_csrf = _csrf,
nonce = nonce,
session = session,
content = @`
    @if(session.user() == null)
        <button><a href="/login">Login</a></button>
    @endif
    @if(session.user() != null)
        <h1>Payment with Stripe</h1>
        <form action='/charge' method='POST' id='checkout-form'>
            <input type='hidden' name="amount" value="${amount}" />
            <input type='hidden' name="currency" value="${currency}" />
            <input type='hidden' name="description" value="Book sharing platform checkout" />
            <input type='hidden' name="${_csrf.getParameterName()}" value="${_csrf.getToken()}">
            <label>Price : <span>${amount / 100}</span></label>
            <script
                    src='https://checkout.stripe.com/checkout.js'
                    class='stripe-button'
                    data-key='${stripePublicKey}'
                    data-amount='${amount}'
                    data-currency='${currency}'
                    data-name='BooqIn'
                    data-description='Book sharing platform checkout'
                    data-locale='auto'
                    data-zip-code='false'>
            </script>
        </form>
        <br/>
        <br/>
        <br/>
        <br/>
    @endif

`)