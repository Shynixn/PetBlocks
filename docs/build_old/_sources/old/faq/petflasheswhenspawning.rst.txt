The pet flashes or does not spawn correctly on fresh a installment?
===================================================================

Sometimes your server does tick a bit different than PetBlocks does expect it, however
this is not a problem at all as you can simply adjust the config settings to fix this problem.

Set all aging options to 0 to fix this.

**config.yml**
::
    ############################

    # Pet settings

    # Settings to customize the pet itself

    # age-small-ticks:  Amount of ticks until the pet grows up.
    # age-large-ticks: Amount of ticks until the pet dies.
    # age-max-ticks: Amount of ticks until the pet stops aging.
    # age-death-on-maxticks: Should the pet die after it has reached the max amount of ticks?

    ############################

    pet:
      age:
        small-ticks: 0
        large-ticks: 0
        max-ticks: 0
        death-on-maxticks: false