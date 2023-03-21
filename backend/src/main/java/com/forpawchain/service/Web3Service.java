package com.forpawchain.service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.springframework.stereotype.Service;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Contract;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.Transfer;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import com.forpawchain.MyContract;
import com.forpawchain.domain.dto.request.LicenseReqDto;
import com.forpawchain.domain.entity.DoctorLicenseEntity;
import com.forpawchain.domain.entity.PetEntity;
import com.forpawchain.domain.entity.UserEntity;
import com.forpawchain.repository.DoctorLicenseRepository;
import com.forpawchain.repository.PetRepository;
import com.forpawchain.repository.UserRepository;

import lombok.RequiredArgsConstructor;
// import org.web3j.protocol.Web3j;
// import org.web3j.protocol.core.DefaultBlockParameter;
// import org.web3j.protocol.core.methods.response.EthAccounts;
// import org.web3j.protocol.core.methods.response.EthBlockNumber;
// import org.web3j.protocol.core.methods.response.EthGetBalance;
// import org.web3j.protocol.core.methods.response.EthGetTransactionCount;

// @RequiredArgsConstructor
@Service
@RequiredArgsConstructor
public class Web3Service {
	private final DoctorLicenseRepository doctorLicenseRepository;
	private final UserRepository userRepository;
	private final PetRepository petRepository;

	private final String NETWORK = "http://3.39.235.238:8545";
	private Web3j web3j = Web3j.build(new HttpService(NETWORK));
	private long CHAINID = 2424L;
	private String fromPrivateKey = "32f246287b10f0e9ac71f6655047b35431f125f97abee915d0244d1cdd74f758";
	private Credentials credentials = Credentials.create(fromPrivateKey);
	// 트랜잭션 매니저 생성
	private TransactionManager transactionManager = new RawTransactionManager(
		web3j, credentials, CHAINID);

	/**
	 * 현재 블록 번호
 	 */
	public EthBlockNumber getBlockNumber() throws ExecutionException, InterruptedException {
		return web3j.ethBlockNumber().sendAsync().get();
	}

	/**
	 * 스마트 컨트랙트 배포
	 */
	public void deployContract(String pid) throws Exception {

		MyContract contract = MyContract.deploy(
			web3j,
			transactionManager,
			DefaultGasProvider.GAS_PRICE,
			DefaultGasProvider.GAS_LIMIT
		).send();

		String ca = contract.getContractAddress();
		System.out.println("컨트랙트 주소 : " + ca);

		//배포된 컨트랙트 주소를 Pet DB에 저장
		// PetEntity petEntity = petRepository.findByPid(pid);
		// petEntity.updatePetCa(ca);
		// petRepository.save(petEntity);
	}

	/**
	 * 의사가 맞는지 확인
	 */
	public boolean checkLicense(LicenseReqDto licenseReqDto) {
		String name = licenseReqDto.getName();
		String registnum = licenseReqDto.getRegistnum();
		String tel = licenseReqDto.getTel();
		int telecom = licenseReqDto.getTelecom();
		Optional<DoctorLicenseEntity> findDoctorLicense = doctorLicenseRepository.findByNameAndRegistnumAndTelAndTelecom(
			name, registnum, tel, telecom);

		if (findDoctorLicense.isPresent()) {
			return true;
		} else {
			return false;
		}
	}

	// // 지정된 주소의 계정
	// public EthAccounts getEthAccounts() throws ExecutionException, InterruptedException {
	// 	System.out.println("------------");
	// 	System.out.println(web3j.ethAccounts().sendAsync());
	// 	System.out.println("------------");
	// 	return web3j.ethAccounts().sendAsync().get();
	// }

	/**
	 * 지갑 생성
	 * private key만 있으면 사용 가능.
	 * private key를 프론트에 전달해주기. db에는 저장 안함
	 */
	public String createWallet(long uid, LicenseReqDto licenseReqDto) throws
		CipherException,
		IOException,
		InvalidAlgorithmParameterException,
		NoSuchAlgorithmException,
		NoSuchProviderException {

		String privateKey = null;

		// 의사 계정이 맞는지 확인
		if (checkLicense(licenseReqDto)) {


			// Generate a new wallet file using a password
			String password = "myPassword";
			String fileName = WalletUtils.generateNewWalletFile(password,
				new File("\\C:\\Users\\SSAFY\\Desktop\\wallet"));
				// new File("\\home\\ubuntu\\dev\\eth\\keystore"));

			// Load the wallet from file using the password
			String walletFilePath = "C:\\Users\\SSAFY\\Desktop\\wallet\\" + fileName;
			// String walletFilePath = "\\home\\ubuntu\\dev\\eth\\keystore" + fileName;
			Credentials myCredentials = WalletUtils.loadCredentials(password, walletFilePath);

			// Print the wallet address
			System.out.println("Wallet address: " + myCredentials.getAddress());

			// 지갑 주소를 DB에 저장
			UserEntity userEntity = userRepository.findByUid(uid);
			userEntity.updateWa(myCredentials.getAddress());
			userRepository.save(userEntity);

			// 지갑의 프라이빗 키
			privateKey = myCredentials.getEcKeyPair().getPrivateKey().toString(16);
		}

		return privateKey;
	}

	public void sendEth(String toAddress) throws Exception {
		// 목적지 주소로 보내려는 이더의 양
		BigInteger value = Convert.toWei("1.0", Convert.Unit.ETHER).toBigInteger();
		
		// 트랜잭션 전송
		EthSendTransaction ethSendTransaction = transactionManager.sendTransaction(DefaultGasProvider.GAS_PRICE,
			DefaultGasProvider.GAS_LIMIT, toAddress, "",
			value);

		// System.out.println(ethSendTransaction.getTransactionHash());
	}

	/**
	 * 해당 유저의 지갑 주소를 DB에서 조회한다.
	 * @param uid
	 */
	public String getAddress(long uid) {
		UserEntity userEntity = userRepository.findByUid(uid);
		return userEntity.getWa();
	}

	/////////////////////////////////////////////
	// // 계좌 거래 건수
	// public EthGetTransactionCount getTransactionCount() throws ExecutionException, InterruptedException {
	// 	EthGetTransactionCount result = new EthGetTransactionCount();
	// 	result = web3j.ethGetTransactionCount(WALLET_ADDRESS,
	// 			DefaultBlockParameter.valueOf("latest"))
	// 		.sendAsync()
	// 		.get();
	// 	return result;
	// }
	//
	// // 계정 잔액
	// public EthGetBalance getEthBalance() throws ExecutionException, InterruptedException {
	// 	return web3j.ethGetBalance(WALLET_ADDRESS,
	// 			DefaultBlockParameter.valueOf("latest"))
	// 		.sendAsync()
	// 		.get();
	// }

	// 스마트컨트랙트명 가져오기
	// public String getContractName() throws Exception {
	// 	return nft.name().send();
	// }

	// 이더리움 블록체인에서 발생하는 이벤트를 필터링하는데 사용(여기에서는 Transfer(거래)만 허용)
	// private EthFilter getEthFilter() throws Exception {
	// 	EthBlockNumber blockNumber = getBlockNumber();
	// 	EthFilter ethFilter = new EthFilter(DefaultBlockParameter.valueOf(blockNumber.getBlockNumber()), DefaultBlockParameterName.LATEST, CONTRACT_ADDRESS);
	//
	// 	Event event = new Event("Transfer",
	// 		Arrays.asList(
	// 			new TypeReference<Address>(true) {
	// 				// from
	// 			},
	// 			new TypeReference<Address>(true) {
	// 			},
	// 			new TypeReference<Uint256>(false) {
	// 				// amount
	// 			}
	// 		));
	// 	String topicData = EventEncoder.encode(event);
	// 	ethFilter.addSingleTopic(topicData);
	// 	ethFilter.addNullTopic();// filter: event type (topic[0])
	// 	//ethFilter.addOptionalTopics("0x"+ TypeEncoder.encode(new Address("")));
	//
	// 	return ethFilter;
	// }
}
