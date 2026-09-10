package ai.testzombie.appium;

record MutationLevel(
        int value,
        String password,
        String passwordConfirm,
        String passwordNext,
        String firstName,
        String lastName,
        String email,
        String personNext,
        String enterprisePlan,
        String planSelector,
        String planNext,
        String termsCss,
        String finishCss
) {
    static MutationLevel of(int level) {
        return switch (level) {
            case 0 -> new MutationLevel(0,
                    "password-input", "password-confirm-input", "password-next",
                    "first-name-input", "last-name-input", "email-input", "person-next",
                    "plan-enterprise", "plan-selector", "plan-next",
                    "terms", "finishButton");
            case 1 -> changed(1, false, false);
            case 2 -> changed(2, false, false);
            case 3 -> changed(3, true, false);
            case 4 -> changed(4, true, true);
            default -> throw new IllegalArgumentException("Mutation Level muss zwischen 0 und 4 liegen: " + level);
        };
    }

    private static MutationLevel changed(int level, boolean structural, boolean componentChange) {
        String password = structural ? "credential-secret" : "account-password";
        String passwordConfirm = structural ? "credential-confirmation" : "account-password-repeat";
        String passwordNext = structural ? "credential-submit" : "access-continue";
        String firstName = structural ? "identity-given" : "given-name-field";
        String lastName = structural ? "identity-family" : "family-name-field";
        String email = structural ? "identity-email" : "contact-email-field";
        String personNext = structural ? "identity-submit" : "identity-continue";
        String enterprise = structural ? "offer-enterprise" : "package-enterprise";
        String selector = structural ? "offer-dropdown" : "package-selector";
        String planNext = structural ? "offer-review" : "package-summary";
        return new MutationLevel(level, password, passwordConfirm, passwordNext,
                firstName, lastName, email, personNext, enterprise, selector, planNext,
                "[data-qa='confirmation-check']", "[data-qa='finish-action']");
    }

    boolean usesPlanDropdown() {
        return value == 4;
    }
}
