#include <stdio.h>			
#include <gmp.h>


#define RAND_NUM_SIZE 512

void specialTotient(mpz_t totient, mpz_t p, mpz_t q) {
	
	mpz_sub_ui(p, p, 1);
	mpz_sub_ui(q, q, 1);
	
	mpz_mul(totient, p, q);
	
	mpz_add_ui(p, p, 1);
	mpz_add_ui(q, q, 1);
	
}

void getCiphertext(mpz_t ciphertext, mpz_t g, unsigned long int m, 
		mpz_t r, mpz_t n, mpz_t n_squared) {
	
	mpz_t gRaisedm, rRaisedn, mod, second_half;
	mpz_init(gRaisedm);
	mpz_init(rRaisedn);
	mpz_init(mod);
	mpz_init(second_half);
	
	
	mpz_pow_ui(gRaisedm, g, m);
	
	mpz_set(ciphertext, gRaisedm);
	mpz_mod(ciphertext, ciphertext, n_squared);
	
	mpz_powm(second_half, r, n, n_squared);
	
	mpz_mul(ciphertext, ciphertext, second_half);
	
	// mod the product by n^2
	mpz_mod(ciphertext, ciphertext, n_squared);

	// clear variables!
}

unsigned long int getMessage(mpz_t ciphertext, mpz_t totient, 
		mpz_t mu, mpz_t n, mpz_t n_squared) {
	
	mpz_t message;
	mpz_init(message);
	
	mpz_powm(message, ciphertext, totient, n_squared);
	
	// L function:
	mpz_sub_ui(message, message, 1);
	mpz_divexact(message, message, n);
	
	
	mpz_mul(message, message, mu);
	mpz_mod(message, message, n);
	
	return mpz_get_ui(message);
	
}

int main(int argc, char *argv[]) {
	
	mpz_t p, q, n, n_squared, g;
	mpz_t totient, mu, r, gcd, ciphertext;
	//mpz_t decrypted;
	unsigned long int seed, message, decrypted;
	int success, r_generated;
	
	gmp_randstate_t r_state;
	seed = 13579;
	
	gmp_randinit_default(r_state);
	gmp_randseed_ui(r_state, seed);
	
	mpz_init(p);
	mpz_init(q);
	mpz_init(n);
	mpz_init(g);
	mpz_init(totient);
	mpz_init(mu);
	
	mpz_init(r);
	mpz_init(gcd);
	mpz_init(n_squared);
	mpz_init(ciphertext);
	
	// calculates prime numbers p, q
	mpz_urandomb(p, r_state, RAND_NUM_SIZE);
	mpz_urandomb(q, r_state, RAND_NUM_SIZE);
	mpz_nextprime(p, p);
	mpz_nextprime(q, q);
	
	// calculates n and g
	mpz_mul(n, p, q);
	mpz_add_ui(g, n, 1);
	
	// calculates totient;
	specialTotient(totient, p, q);
	
	// calculates mu
	success = mpz_invert(mu, totient, n);
	if (success)
		printf("Mu calculation successful\n");
		
	// generates r
	// careful, different random function!
	r_generated = 0;
	while (!r_generated) {
		// generates potential r
		mpz_urandomm(r, r_state, n);
		mpz_gcd(gcd, r, n);
		success = mpz_cmp_ui(gcd, 1);
		// we want mpz_cmp_ui() to return 0
		if (!success)
			r_generated = 1;
	}	
	
	// calculates n^2
	mpz_mul(n_squared, n, n);
	
	message = 12;
	
	// calculates ciphertext
	
	getCiphertext(ciphertext, g, message, r, n, n_squared);
	
	
	// gets message
	decrypted = getMessage(ciphertext, totient, mu, n, n_squared);
	
	gmp_printf("MESSAGE: %d\n\n\n", message);
	
	gmp_printf("p: %Zd\n", p);
	gmp_printf("q: %Zd\n", q);
	gmp_printf("n: %Zd\n", n);
	gmp_printf("totient: %Zd\n", totient);
	gmp_printf("mu: %Zd\n", mu);
	gmp_printf("r: %Zd\n", r);
	
	gmp_printf("ciphertext: %Zd\n", ciphertext);
	printf("decrypted: %lu\n", decrypted);
	
	/*
	gmp_randclear(r_state);
    mpz_clear(rand_Num);
    */
    return 0;
}

