# Cardinal Introductory Tutorial

This repository contains the example code used in the [introductory tutorial](https://docs.icure.com/tutorial/basic/sdk-basic-tutorial).
It showcases several self-contained use cases that highlight the main features of the Cardinal SDK, including:

- Creating a patient.
- Generating medical data for that patient (simulating a medical examination with multiple exams).
- Searching through the medical data.
- Sharing the medical data with other doctors and patients.
- Utilizing codifications such as [SNOMED-CT](https://www.snomed.org/what-is-snomed-ct) and [LOINC](https://loinc.org/).

The tutorial code is available in Kotlin, Python, and TypeScript. Below you will find instructions for running the code 
in all three languages. For further explanations and examples, check the [Cardinal documentation](https://docs.icure.com).

## Launching the Tutorial in Kotlin

To run the tutorial in Kotlin, clone this repository:

```bash
git clone https://github.com/icure/cardinal-introductory-tutorial.git
```

Then, open the folder in IntelliJ and run the [main function](https://github.com/icure/cardinal-introductory-tutorial/blob/main/kotlin/src/main/kotlin/com/cardinal/Main.kt)

## Launching the Tutorial in Python

To run the tutorial in Python, clone this repository:

```bash
git clone https://github.com/icure/cardinal-introductory-tutorial.git
cd cardinal-introductory-tutorial
```

It is recommended to use a virtual environment, to avoid conflicting dependencies. The minimum supported Python version is
3.9.

```bash
cd python
python3 -m venv venv
source venv/bin/activate
```

Then, install the Cardinal SDK from PyPI.

```bash
pip install cardinal-sdk
```

Finally, run the example Python code.

```bash
python src/main.py
```

## Launching the Tutorial in Typescript

To run the tutorial in Typescript, clone this repository:

```bash
git clone https://github.com/icure/cardinal-introductory-tutorial.git
cd cardinal-introductory-tutorial
```

The minimum supported Node version is 19. You can install it using [nvm](https://github.com/nvm-sh/nvm).

```bash
nvm install 19
nvm use 19
```

Then, you can navigate to the `typescript` directory and install the required dependencies using [yarn](https://yarnpkg.com/).

```bash
cd typescript
yarn install
```

Finally, run the example Typescript code using yarn:

```bash
yarn ts-node --esm src/main.ts
```