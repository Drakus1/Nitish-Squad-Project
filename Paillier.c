#include <stdio.h>
#include <stdlib.h>			
#include <gmp.h>


static int RAND_NUM_SIZE;

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

unsigned long int decrypt(mpz_t ciphertext, mpz_t totient, 
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

/*
void encrypt(mpz_t ciphertext, unsigned long int message) {
}
*/

int main(int argc, char *argv[]) {
	
	mpz_t p, q, n, n_squared, g;
	mpz_t totient, mu, r, gcd, ciphertext1, ciphertext2;
	// product of ciphertexts
	mpz_t product, exponent;

	unsigned long int seed, message1, message2, decrypted1, decrypted2, decrypted_product;
	unsigned long int decrypted_exponent;
	unsigned int k = 4;
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
	mpz_init(ciphertext1);
	mpz_init(ciphertext2);
	
	mpz_init(product);
	mpz_init(exponent);
	
	RAND_NUM_SIZE = atoi(argv[1]);
	printf("\nChoose p and q of %d bits\n", RAND_NUM_SIZE);
	
		
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
	
	// gets message
	message1 = atoi(argv[2]);
	message2 = atoi(argv[3]);
	
	// calculates ciphertext
	getCiphertext(ciphertext1, g, message1, r, n, n_squared);
	getCiphertext(ciphertext2, g, message2, r, n, n_squared);
	
	
	mpz_pow_ui(exponent, ciphertext1, k);
	mpz_mul(product, ciphertext1, ciphertext2);
	
	
	// gets message
	decrypted1 = decrypt(ciphertext1, totient, mu, n, n_squared);
	decrypted2 = decrypt(ciphertext2, totient, mu, n, n_squared);
	decrypted_product = decrypt(product, totient, mu, n, n_squared);
	decrypted_exponent = decrypt(exponent, totient, mu, n, n_squared);
	
	
	gmp_printf("MESSAGE: %d\n\n", message1);
	gmp_printf("MESSAGE: %d\n\n", message2);
	
	
	gmp_printf("p: %Zd\n\n", p);
	gmp_printf("q: %Zd\n\n", q);
	gmp_printf("n: %Zd\n\n", n);
	gmp_printf("totient: %Zd\n\n", totient);
	gmp_printf("mu: %Zd\n\n", mu);
	gmp_printf("r: %Zd\n\n\n", r);
	
	gmp_printf("ciphertext: %Zd\n\n", ciphertext1);
	printf("decrypted: %lu\n\n", decrypted1);
	printf("decrypted: %lu\n\n", decrypted2);
	printf("sum of message1 + message2: %lu\n\n", decrypted_product);
	printf("4 times message1: %lu\n\n", decrypted_exponent);
	printf("\n\n");
	
	
	gmp_randclear(r_state);
    
    mpz_clear(p);
	mpz_clear(q);
	mpz_clear(n);
	mpz_clear(g);
	mpz_clear(totient);
	mpz_clear(mu);
	
	mpz_clear(r);
	mpz_clear(gcd);
	mpz_clear(n_squared);
	mpz_clear(ciphertext1);
	mpz_clear(ciphertext2);
    
    return 0;
}

